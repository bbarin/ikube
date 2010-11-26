package ikube.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.apache.log4j.Logger;

public abstract class Handler implements IHandler<Indexable<?>> {

	protected Logger logger;

	private IHandler<Indexable<?>> prev;
	private IHandler<Indexable<?>> next;
	private int threads;

	public Handler(IHandler<Indexable<?>> previous) {
		this.prev = previous;
		if (this.prev != null) {
			((Handler) previous).setNext(this);
		}
		this.logger = Logger.getLogger(this.getClass());
	}

	@Override
	public void handle(IndexContext indexContext, Indexable<?> indexable) throws Exception {
		if (this.next != null) {
			this.next.handle(indexContext, indexable);
		}
	}

	private void setNext(IHandler<Indexable<?>> next) {
		this.next = next;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

}
