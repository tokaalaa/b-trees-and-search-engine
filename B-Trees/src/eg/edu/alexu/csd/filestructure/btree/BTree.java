package eg.edu.alexu.csd.filestructure.btree;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

public class BTree<K extends Comparable <K>,V> implements IBTree<K,V>{
	private int minDegree;
	private BTreeNode<K,V> root = new BTreeNode<K,V> ();
	
	@SuppressWarnings("hiding")
	private class BTreeNode<K extends Comparable<K>, V> implements IBTreeNode<K, V>  {
	    private int numOfKeys = 0;
	    private boolean isLeaf = true;
	    private List<K> keys = new ArrayList<K>();
	    private List<V> values = new ArrayList<V>();
	    private List<IBTreeNode<K, V>> children = new ArrayList<IBTreeNode<K, V>>();
	    
		@Override
		public int getNumOfKeys() {
			// TODO Auto-generated method stub
			return numOfKeys;
		}

		@Override
		public void setNumOfKeys(int numOfKeys) {
			// TODO Auto-generated method stub
			this.numOfKeys = numOfKeys;
		}

		@Override
		public boolean isLeaf() {
			// TODO Auto-generated method stub
			return isLeaf;
		}

		@Override
		public void setLeaf(boolean isLeaf) {
			// TODO Auto-generated method stub
			this.isLeaf = isLeaf;
		}

		@Override
		public List<K> getKeys() {
			// TODO Auto-generated method stub
			return keys;
		}

		@Override
		public void setKeys(List<K> keys) {
			// TODO Auto-generated method stub
			this.keys = keys;
		}

		@Override
		public List<V> getValues() {
			// TODO Auto-generated method stub
			return values;
		}

		@Override
		public void setValues(List<V> values) {
			// TODO Auto-generated method stub
			this.values = values;
		}

		@Override
		public List<IBTreeNode<K, V>> getChildren() {
			// TODO Auto-generated method stub
			return children;
		}

		@Override
		public void setChildren(List<IBTreeNode<K, V>> children) {
			// TODO Auto-generated method stub
			this.children = children;
		}
		
		private boolean remove(K key) {
			int idx = findKey(key);
		    // The key to be removed is present in this node 
			if (idx < numOfKeys && keys.get(idx).equals(key))
			{
				if (isLeaf)
				{
					removeFromLeaf(idx);
				}
				else
				{
					removeFromNonLeaf(idx);
				}
			}
			else {
				if (isLeaf)
				{
					return false;
				}
				boolean flag = ((idx == numOfKeys)? true : false);
				// If the child where the key is supposed to exist has less that t keys, 
		        // we fill that child
				if (children.get(idx).getNumOfKeys() < minDegree)
				{
					fill(idx);
				}
				// If the last child has been merged, it must have merged with the previous 
		        // child and so we recurse on the (idx-1)th child. Else, we recurse on the 
		        // (idx)th child which now has atleast t keys
				if (flag && idx > numOfKeys)
				{
					return ((BTree<K, V>.BTreeNode<K, V>) children.get(idx - 1)).remove(key);
				}
				else
				{
					return ((BTree<K, V>.BTreeNode<K, V>) children.get(idx)).remove(key);
				}
			}
			return true;
		}
		
		
		private int findKey( K key)
		{
			int idx = 0;
			while (idx < numOfKeys && (keys.get(idx).compareTo(key) < 0))
			{
				++idx;
			}
			return idx;
		}

		private void removeFromLeaf(int idx)
		{
		for (int i = idx + 1; i < numOfKeys; ++i)
		{
			keys.set(i - 1, keys.get(i));
			values.set(i - 1, values.get(i));
		}
		numOfKeys--;
		return;
	}

		private void removeFromNonLeaf(int idx)
		{
		K k = keys.get(idx);
		 if (children.get(idx).getNumOfKeys() >= minDegree)
		{
			IBTreeNode<K, V> pred = getPred(idx);
			keys.set(idx, pred.getKeys().get(pred.getNumOfKeys() - 1));
			values.set(idx, pred.getValues().get(pred.getNumOfKeys() - 1));
			((BTree<K, V>.BTreeNode<K, V>) children.get(idx)).remove(pred.getKeys().get(pred.getNumOfKeys() - 1));
		}
		 else if (children.get(idx + 1).getNumOfKeys() >= minDegree)
		{
			 IBTreeNode<K, V> succ = getSucc(idx);
			 keys.set(idx, succ.getKeys().get(0));
			 values.set(idx, succ.getValues().get(0));
			((BTree<K, V>.BTreeNode<K, V>) children.get(idx + 1)).remove(succ.getKeys().get(0));
		} 
		else
		{
			merge(idx);
			((BTree<K, V>.BTreeNode<K, V>) children.get(idx)).remove(k);
		}
		return;
	}

		private IBTreeNode<K, V> getPred(int idx)
		{
		IBTreeNode<K, V> cur = children.get(idx);
		while (!cur.isLeaf())
		{
			cur = cur.getChildren().get(cur.getNumOfKeys());
		}
		return cur;
	}

	private IBTreeNode<K, V> getSucc(int idx)
	{
		IBTreeNode<K, V> cur = children.get(idx + 1);
		while (!cur.isLeaf())
		{
			cur = cur.getChildren().get(0);
		}
		return cur;
	}

	private void fill(int idx)
	{
		if (idx != 0 && children.get(idx - 1).getNumOfKeys() >= minDegree)
		{
			borrowFromPrev(idx);
		}
		else if (idx != numOfKeys && children.get(idx + 1).getNumOfKeys() >= minDegree)
		{
			borrowFromNext(idx);
		}
		else
		{
			if (idx != numOfKeys )
			{
				merge(idx);
			}
			else
			{
				merge(idx - 1);
			}
		}
		return;
	}

	private void borrowFromPrev(int idx)
	{
		IBTreeNode<K, V> child = children.get(idx);
		IBTreeNode<K, V> sibling = children.get(idx - 1);
		child.getKeys().add(child.getNumOfKeys(), child.getKeys().get(child.getNumOfKeys() - 1));
		child.getValues().add(child.getNumOfKeys(), child.getValues().get(child.getNumOfKeys() - 1));
		
		for (int i = child.getNumOfKeys() - 2; i >= 0; --i)
		{
			child.getKeys().set(i+1, child.getKeys().get(i));
			child.getValues().set(i+1, child.getValues().get(i));

		}
		if (!child.isLeaf())
		{
			child.getChildren().add(child.getNumOfKeys() + 1, child.getChildren().get(child.getNumOfKeys()));
			
			for (int i = child.getNumOfKeys() - 1; i >= 0; --i)
			{
				child.getChildren().set(i+1, child.getChildren().get(i));
			}
		}
		child.getKeys().set(0, keys.get(idx - 1));
		child.getValues().set(0, values.get(idx - 1));

		if (!child.isLeaf())
		{
			child.getChildren().set(0, sibling.getChildren().get(sibling.getNumOfKeys()));
		}
		keys.set(idx - 1, sibling.getKeys().get(sibling.getNumOfKeys() - 1));
		values.set(idx - 1, sibling.getValues().get(sibling.getNumOfKeys() - 1));
		
		child.setNumOfKeys(child.getNumOfKeys() + 1);
		sibling.setNumOfKeys(sibling.getNumOfKeys() - 1);
		return;
	}

	private void borrowFromNext(int idx)
	{
		IBTreeNode<K, V> child = children.get(idx);
		IBTreeNode<K, V> sibling = children.get(idx + 1);
		
		child.getKeys().add(child.getNumOfKeys(), keys.get(idx));
		child.getValues().add(child.getNumOfKeys(), values.get(idx));

		if (!child.isLeaf())
		{
			child.getChildren().add(child.getNumOfKeys() + 1, sibling.getChildren().get(0));
		}
		keys.set(idx, sibling.getKeys().get(0));
		values.set(idx, sibling.getValues().get(0));

		for (int i = 1; i < sibling.getNumOfKeys(); ++i)
		{
			sibling.getKeys().set(i - 1, sibling.getKeys().get(i));
			sibling.getValues().set(i - 1, sibling.getValues().get(i));
		}
		
		if (!sibling.isLeaf())
		{
			for (int i = 1; i <= sibling.getNumOfKeys(); ++i)
			{
				sibling.getChildren().set(i - 1, sibling.getChildren().get(i));
			}
		}
		child.setNumOfKeys(child.getNumOfKeys() + 1);
		sibling.setNumOfKeys(sibling.getNumOfKeys() - 1);
		return;
	}

	public void merge(int idx)
	{
		IBTreeNode<K, V> child = children.get(idx);
		IBTreeNode<K, V> sibling = children.get(idx + 1);
		child.getKeys().add(minDegree - 1, keys.get(idx));
		child.getValues().add(minDegree - 1, values.get(idx));

		for (int i = 0; i < sibling.getNumOfKeys(); ++i)
		{ 
			if ((minDegree + i) >= child.getNumOfKeys()) {
				child.getKeys().add(minDegree + i, sibling.getKeys().get(i));
				child.getValues().add(minDegree + i, sibling.getValues().get(i));
			} else {
				child.getKeys().set(minDegree + i, sibling.getKeys().get(i));
				child.getValues().set(minDegree + i, sibling.getValues().get(i));
			}
		}
		if (!child.isLeaf())
		{
			for (int i = 0; i <= sibling.getNumOfKeys(); ++i)
			{
				if (minDegree + i > child.getNumOfKeys()) {
					child.getChildren().add(i + minDegree, sibling.getChildren().get(i));
				}else {
					child.getChildren().set(i + minDegree, sibling.getChildren().get(i));
				}
			}
		}
		for (int i = idx + 1; i < numOfKeys; ++i)
		{
			keys.set(i - 1, keys.get(i));
			values.set(i - 1, values.get(i));
		}
		for (int i = idx + 2; i <= numOfKeys; ++i)
		{
			children.set(i - 1, children.get(i));
		}
		child.setNumOfKeys(child.getNumOfKeys() + sibling.getNumOfKeys() + 1);

		numOfKeys--;		
		return;
	}

	}

	public BTree(int d) {
		minDegree = d;
		if (d < 2) {
			throw new RuntimeErrorException(null);
		}
	}
	@Override
	public int getMinimumDegree() {
		// TODO Auto-generated method stub
		return minDegree;
	}

	@Override
	public IBTreeNode<K, V> getRoot() {
		// TODO Auto-generated method stub
		if(root == null) {
			return null;
		}
		if(root.getKeys().size() == 0) {
			return null;
		}
		return root;
	}

	@Override
	public void insert(K key, V value) {
		// TODO Auto-generated method stub
		if (key == null || value == null) {
			throw new RuntimeErrorException(null);
		}
		if(search(key) == null) {
			IBTreeNode<K,V> r;
			r = root;	
			if(r.getNumOfKeys() == (2 * minDegree - 1)) {
				IBTreeNode<K,V> s = new BTreeNode<K,V>();
				root = (BTree<K, V>.BTreeNode<K, V>) s;
				s.setLeaf(false);
				s.setNumOfKeys(0);
				ArrayList<IBTreeNode<K, V>> children = new ArrayList<IBTreeNode<K, V>>();
				children.add(r);
				s.setChildren(children);
				splitChild(s, 1);
				insertNonFull(s, key, value);
			}else insertNonFull(r, key, value);
		}
	}
	
	private void insertNonFull(IBTreeNode<K, V> x, K k, V v) {
		// TODO Auto-generated method stub
	int i = x.getNumOfKeys();
	if(x.isLeaf()) {
		while(i >= 1 && k.compareTo(x.getKeys().get(i - 1)) < 0) {
			i--;
		}
		x.getKeys().add(i, k);
		x.getValues().add(i, v);
		x.setNumOfKeys(x.getNumOfKeys()+1);
	}else {
		while(i>=1 && k.compareTo(x.getKeys().get(i - 1)) < 0) {
			i--;
		}
		i++;
		if(x.getChildren().get(i-1).getNumOfKeys() == 2* minDegree - 1) {
			splitChild(x,i);
			if(k.compareTo(x.getKeys().get(i - 1)) > 0) {
				i++;
			}
		}
		insertNonFull(x.getChildren().get(i-1),k,v);
	}
	}
	
	private void splitChild(IBTreeNode<K, V> x, int i) {

	IBTreeNode<K, V> z = new BTreeNode<K, V>();
	IBTreeNode<K, V> y = x.getChildren().get(i-1);
	z.setLeaf(y.isLeaf());
	z.setNumOfKeys(minDegree - 1);
	ArrayList<K> zkeys = new ArrayList<K>();
	ArrayList<V> zvalues = new ArrayList<V>();
	for(int j = 1; j <= (minDegree - 1); j++ ) {
		zkeys.add(y.getKeys().remove(minDegree));
		zvalues.add(y.getValues().remove(minDegree));
	}
	z.setKeys(zkeys);
	z.setValues(zvalues);
	
	ArrayList<IBTreeNode<K, V>> zchildren = new ArrayList<IBTreeNode<K, V>>();
	if(!y.isLeaf()) {
		for(int j = 1; j <= minDegree; j++) {
			zchildren.add(y.getChildren().remove(minDegree));
		}
		z.setChildren(zchildren);
	}
	
	y.setNumOfKeys(minDegree - 1);

	x.getChildren().add(i, z);
	x.getKeys().add(i-1, y.getKeys().get(minDegree - 1));
	x.getValues().add(i-1, y.getValues().get(minDegree - 1));
	y.getKeys().remove(minDegree-1);
	y.getValues().remove(minDegree-1);
	x.setNumOfKeys(x.getNumOfKeys() + 1);
}
	
	@Override
	public V search(K key) {
	// TODO Auto-generated method stub
	if (key == null) {
		throw new RuntimeErrorException(null);
	}
	IBTreeNode<K,V> node = root;
	if(node == null) {
	return null;	
	}
	return search(node, key);
}

	private V search(IBTreeNode<K,V> node, K key) {
	int i = 1;
	//System.out.println(node.getNumOfKeys());
	while((i <= node.getNumOfKeys()) && (key.compareTo(node.getKeys().get(i-1)) > 0)) {
		i = i + 1;
	}
	if ((i <= node.getNumOfKeys()) && (key.compareTo(node.getKeys().get(i-1)) == 0))
		return node.getValues().get(i-1);
	else if(node.isLeaf())
		return null;
	return search(node.getChildren().get(i-1), key);
}

	@Override
	public boolean delete(K key) {
	// TODO Auto-generated method stub
	if (key == null) {
		throw new RuntimeErrorException(null);
	}
	if (search(key) == null) {
		return false;
	}
	// Call the remove function for root
	boolean flag = root.remove(key);

	// If the root node has 0 keys, make its first child as the new root
	if (root.getNumOfKeys() == 0)
	{
		if (root.isLeaf())
		{
			root = null;
		}
		else
		{
			root = (BTree<K, V>.BTreeNode<K, V>) root.getChildren().get(0);
		}
	}

	if(!flag) {
		return false;
	}
	return true;
}

	
}
