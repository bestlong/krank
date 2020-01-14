package org.crank.crud.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.crank.core.RequestParameterMapFinder;
import org.crank.core.LogUtils;
import org.crank.crud.controller.datasource.PagingDataSource;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("serial")
public class Paginator implements Pageable, Serializable {

    protected int currentPage=0;
    protected int itemsPerPage=10;
    protected int startItemIndexCurrentPage=0;
    protected int numberOfPages;
    protected int maxItemsPerPage=500;
    @SuppressWarnings("unchecked")
	protected PagingDataSource dataSource;
    protected int count;
    protected List<Integer> pageNumberList;
    protected RequestParameterMapFinder requestParameterMapFinder;
    protected String currentPageParamName = "currentPage";
    protected Logger logger = LogManager.getLogger(Paginator.class);
    protected List<?> page;
    protected boolean initialized;
    public static int NO_ASSUMED_COUNT = -1;
    protected int assumedCount = NO_ASSUMED_COUNT; 


    
    public void setCurrentPageParamName( String currentPageParamName ) {
        this.currentPageParamName = currentPageParamName;
    }

    public Paginator () {
        logger.debug("Paginator()");
    }
    
    @SuppressWarnings("unchecked")
	public Paginator (PagingDataSource dataSource) {
        this.dataSource = dataSource;
        //reset();
    }
    

    public void fastForwardPages() {
        logger.debug("fastForwardPages()");

        currentPage += 5;
        if (currentPage > numberOfPages) {
            currentPage = numberOfPages;
        }
        retrievePage();
        firePagination();
    }

    public void rewindPages() {
        logger.debug("rewindPages()");
        currentPage -= 5;
        if (currentPage < 0) {
            currentPage = 0;
        }
        retrievePage();        
        firePagination();
    }


    public void moveToEndPage() {
        logger.debug("moveToEndPage()");
        if (currentPage < 0) {
            currentPage = 0;
        }
        currentPage = numberOfPages-1;
        retrievePage();        
        firePagination();
    }

    public void moveToStartPage() {
        logger.debug("moveToStartPage()");
        currentPage = 0;
        retrievePage();                
        firePagination();
    }

    public void moveToNextPage() {
        logger.debug("moveToNextPage()");
        currentPage += 1;
        if (currentPage > numberOfPages) {
            currentPage = numberOfPages;
        }
        retrievePage();        
        firePagination();
    }

    public void moveToPreviousPage() {
        logger.debug("moveToPreviousPage()");
        currentPage -= 1;
        if (currentPage < 0) {
            currentPage = 0;
        }
        retrievePage();        
        firePagination();
    }

    private boolean reset = false;
    
    public void reset() {
    	reset = true;
        logger.debug("reset() was called");
        this.count = count();
        this.numberOfPages = count / itemsPerPage;
        if ((count % itemsPerPage) != 0) {
            this.numberOfPages++;
        }
        this.currentPage = 0;
        this.initialized = false;
        LogUtils.debug(logger, "reset() count=%s, itesmPerPage=%s, numberOfPages=%s", count, itemsPerPage, numberOfPages);

    }
    
    protected int count() {
    	if (assumedCount==NO_ASSUMED_COUNT) {
    		return dataSource.getCount();
    	} else {
    		return assumedCount;
    	}
    }


    private void retrievePage() {
        logger.debug("retrievePage()");
        initialized = true;
        if (currentPage < 0) {
            currentPage = 0;
        }
        page = dataSource.list( currentPage * itemsPerPage, itemsPerPage );
    }



    public void moveToPage() {
        logger.debug("moveToPage()");
        String sCurrentPage = "0";
        Object oCurrentPage = requestParameterMapFinder.getMap().get( this.currentPageParamName );
        if (oCurrentPage instanceof String) {
            sCurrentPage = (String) oCurrentPage;
        } else if (oCurrentPage instanceof String[]) {
            sCurrentPage = ((String[])oCurrentPage)[0];
        }
        int currentPage = Integer.parseInt( sCurrentPage );
        moveToPage(currentPage-1);
    }
    
    public void moveToPage( int pageNumber ) {
        LogUtils.debug(logger, "moveToPage(pageNumber=%s)", pageNumber);
        currentPage = pageNumber;
        if (currentPage > numberOfPages) {
            currentPage = numberOfPages;
        } else if (currentPage < 0) {
            currentPage = 0;
        }
        retrievePage();        
        firePagination();
    }


    public boolean isFastForwardPagesEnabled() {
        return (currentPage + 5) < numberOfPages;
    }

    public boolean isRewindPagesEnabled() {
        return (currentPage - 5) >= 0;
    }

    public boolean isMoveToEndPageEnabled() {
        if (numberOfPages == 1) {
            return false;
        } else {
            return (currentPage + 1) != numberOfPages ;
        }
    }

    public boolean isMoveToStartPageEnabled() {

        return currentPage != 0;
    }

    public boolean isMoveToNextPageEnabled() {
        return (currentPage + 1) < numberOfPages;
    }

    public boolean isMoveToPreviousPageEnabled() {
        return (currentPage - 1) >= 0;
    }


    public List<Integer> getPageNumberList() {
    	calcPageNumberList();
        return pageNumberList;
    }

    public String getInit() {
    	if (!reset) {
    		reset();
    	}
    	return "";
    }

    @SuppressWarnings("unchecked")
	public List getPage() {
    	if (!reset) {
    		reset();
    	}
        logger.debug("getPage()");
        if (initialized) {
            logger.debug("getPage() send cached page");
            return page;
        } else {
            retrievePage();
            logger.debug("getPage() send new page");
            return page;
        }
    }

    /**
     * Utility function to initialize the page number list
     */
    private void calcPageNumberList(){
        logger.debug("calcPageNumberList()");

        int start = currentPage - 5;
        if (start < 0) {
            start = 0;
        }
        pageNumberList = new ArrayList<Integer>(10);
        for (int index = 0; index < numberOfPages && index < 10; index++) {
        	pageNumberList.add((start + index + 1));
            if ((start + index + 2) > numberOfPages) {
                break;
            }
        }
    	
    }
    
    
    /**
     * Used to evaluate whether or not to display the fancy last page link delimiter
     */
	public boolean isShowLastPageDelimiter() {
        logger.debug("isShowLastPageDelimiter()");
        if (pageNumberList != null) {
			if (pageNumberList.size() > 0) {
				if (pageNumberList.get(pageNumberList.size()-1) + 1 < numberOfPages) {
					return true;
				}
			}
		}
		return false;
	}

    /**
     * Used to evaluate whether or not to display the All Rows option
     */
	public boolean isAllowAllRows() {
        logger.debug("isAllowAllRows()");
        if ( count > maxItemsPerPage ) {
			return false;
		}
		return true;
	}

    /**
     * Used to evaluate whether or not to display the fancy last page link to the last page
     */
	public boolean isShowLastPageLink() {
        logger.debug("isShowLastPageLink()");
        if (pageNumberList != null) {
			if (pageNumberList.size() > 0) {
				if (pageNumberList.get(pageNumberList.size()-1) < numberOfPages) {
					return true;
				}
			}
		}
		return false;
	}


    public void setRequestParameterMapFinder( RequestParameterMapFinder requestParameterMapFinder ) {
        this.requestParameterMapFinder = requestParameterMapFinder;
    }

    private List<PaginationListener> listeners = new ArrayList<PaginationListener>();

    public void addPaginationListener(PaginationListener listener) {
        listeners.add( listener );
    }
    public void removePaginationListener(PaginationListener listener) {
        listeners.remove( listener );
    }

    /**
     * Fire and event to the listeners.
     *
     */
    private void firePagination() {
        PaginationEvent pe = new PaginationEvent(this, currentPage);
        for (PaginationListener pl : listeners) {
            pl.pagination( pe );
        }
    }

    public int getMaxItemsPerPage() {
		return maxItemsPerPage;
	}

	public void setMaxItemsPerPage(int maxItemsPerPage) {
		this.maxItemsPerPage = maxItemsPerPage;
	}

	public int getNumberOfPages() {
        return numberOfPages;
    }

    public int getStartItemIndexCurrentPage() {
        return startItemIndexCurrentPage;
    }

    @SuppressWarnings("unchecked")
	public void setDataSource( PagingDataSource dataSource ) {
        this.dataSource = dataSource;
    }

    public int getItemsPerPage() {
        return this.itemsPerPage;
    }

    public void setItemsPerPage( final int aItemsPerPage ) {
        if (this.itemsPerPage != aItemsPerPage) {
            this.itemsPerPage = aItemsPerPage;
            reset();
            moveToStartPage();
        }
    }

    public int getCurrentPageNumber() {
        return currentPage;
    }

    public int getPageCount() {
        return numberOfPages;
    }

	public void setAssumedCount(int assumedCount) {
		this.assumedCount = assumedCount;
	}
	
	public boolean isInitialized() {
		return initialized;
	}

}
