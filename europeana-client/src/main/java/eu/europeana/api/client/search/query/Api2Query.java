package eu.europeana.api.client.search.query;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import eu.europeana.api.client.connection.BaseApiConnection;
import eu.europeana.api.client.search.common.EuropeanaFields;

/**
 * 
 * @author Sergiu Gordea
 * 
 */
public class Api2Query extends EuropeanaQuery implements Api2QueryInterface {

	private List<String> queryRefinements;
	private String collectionName;
	
	/**
	 * This attribute is used to set directly the aggregated query parameters, possibly by reusing a portal query
	 *  This must start with query= string as it has the same semantic like the query parameter in the portal search url. 
	 */
	private String queryParams;
    
	/**
	 * Constructor that supports searching objects within a collection
	 * @param collectionName
	 */
	public Api2Query(String collectionName) {
		this.setCollectionName(collectionName);
	}

	public Api2Query() {
		super();
	}
	
	@Override
	public List<String> getQueryRefinements() {
		return queryRefinements;
	}

	@Override
	public void addQueryRefinement(String qf) {
		if(queryRefinements == null)
			queryRefinements = new ArrayList<String>(3);
		
		queryRefinements.add(qf);
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see eu.europeana.api.client.search.query.EuropeanaQuery#getQueryUrl(eu.europeana.api.client.connection.BaseApiConnection, long, long)
	 */
	public String getQueryUrl(BaseApiConnection connection, long limit,
			long offset) throws UnsupportedEncodingException {
				
		StringBuilder url = buildBaseSearchUrl(connection);
		appendSearchQueryParams(url);
		
		if (limit > 0)
			url.append("&rows=").append(limit);
		if (offset > 0)
			url.append("&start=").append(offset);
			
		return url.toString();
	}

	private void appendSearchQueryParams(StringBuilder url) throws UnsupportedEncodingException {
		if(getQueryParams() != null){
			url.append(queryParams);
		}else{
			String searchTerms = getSearchTerms();
			url.append("query=").append(searchTerms);
			appendQueryRefinements(url);			
		}
	}
	
	
	@Override
	public String getQueryUrl(BaseApiConnection connection, String cursor, int rows) throws UnsupportedEncodingException {
		 
		 StringBuilder url = buildBaseSearchUrl(connection);
		 appendSearchQueryParams(url);
		
		 url.append("&cursor=").append(cursor);
		 if(rows >= 0)
			 url.append("&rows=").append(rows);
		 
		return url.toString();
	}
	 
	void appendQueryRefinements(StringBuilder url) throws UnsupportedEncodingException {
		
		if(getQueryRefinements() == null)
			return;
		
		for (String qf : getQueryRefinements()) {
			appendQueryRefinement(url, qf);
		}
		
	}
	
	private void appendQueryRefinement(StringBuilder buf, String qf) throws UnsupportedEncodingException {
    	if (qf == null) {
            return;
        }

    	qf = qf.trim();
        if (qf.length() == 0) {
            return;
        }
        
        buf.append("&qf=");
        
        buf.append(encodeSearchValue(qf));
	}

	@Override
	protected void buildSearchQueryString(StringBuffer buf) {
		super.buildSearchQueryString(buf);
		
		if(getCollectionName() !=null)
			addSearchField(buf, EuropeanaFields.EUROPEANA_COLLECTION_NAME, collectionName);
	}
	
	@Override
	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getQueryParams() {
		return queryParams;
	}

	public void setQueryParams(String queryParams) {
		this.queryParams = queryParams;
	}

	
}
