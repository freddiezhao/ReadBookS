package com.sina.book.control.download;

import com.sina.book.data.Book;

/**
 * The listener interface for receiving ITaskUpdate events.
 * The class that is interested in processing a ITaskUpdate
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addITaskUpdateListener<code> method. When
 * the ITaskUpdate event occurs, that object's appropriate
 * method is invoked.
 *
 * @see
 */
public interface ITaskUpdateListener {
    
    /**
     * 进度更新.
     *
     * @param mustUpdate the must update
     * @param mustRefresh the must refresh
     * @param stateCode the state code
     */
    public void onUpdate(Book book, boolean mustUpdate, boolean mustRefresh, int progress, 
            int stateCode);
    
}
