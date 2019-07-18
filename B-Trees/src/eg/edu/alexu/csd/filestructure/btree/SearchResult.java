package eg.edu.alexu.csd.filestructure.btree;

public class SearchResult implements ISearchResult{
	private String ID;
	private int rank = 0;
	
	public SearchResult(String s, int i) {
		ID = s;
		rank = i;
	}
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return ID;
	}

	@Override
	public void setId(String id) {
		// TODO Auto-generated method stub
		ID = id;
	}

	@Override
	public int getRank() {
		// TODO Auto-generated method stub
		return rank;
	}

	@Override
	public void setRank(int rank) {
		// TODO Auto-generated method stub
		this.rank = rank;
	}

}
