package com.sina.book.control;

public abstract class GenericTask extends AbsNormalAsyncTask<TaskParams, Object, TaskResult> {

    private ITaskFinishListener mTaskFinishListener;
    protected TaskResult mResult = new TaskResult(-1, this, null);
    private String mType;

    public GenericTask(final int threadPriority) {
        super(threadPriority);
    }

    public GenericTask() {
        super();
    }

    @Override
    protected synchronized void onPostExecute(TaskResult result) {
        super.onPostExecute(result);
        
        ITaskFinishListener listener = mTaskFinishListener;
        
        if (listener != null) {
        	listener.onTaskFinished(result);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        // if (mTaskFinishListener != null) {
        // mTaskFinishListener.onTaskFinished(null);
        // }
    }

    public synchronized ITaskFinishListener getTaskFinishListener() {
        return mTaskFinishListener;
    }

    public synchronized void setTaskFinishListener(ITaskFinishListener taskFinishListener) {
        this.mTaskFinishListener = taskFinishListener;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }
}
