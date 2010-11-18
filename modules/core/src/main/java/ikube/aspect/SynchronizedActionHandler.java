package ikube.aspect;

import ikube.cluster.ILockManager;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

public class SynchronizedActionHandler implements ISynchronizedActionHandler {

	private Logger logger;
	private ILockManager lockManager;

	public SynchronizedActionHandler() {
		this.logger = Logger.getLogger(this.getClass());
	}

	@Override
	public synchronized Object execute(final ProceedingJoinPoint call) throws Throwable {
		try {
			// Check that we have the token. This waits for the token to be available
			boolean haveToken = lockManager.haveToken();
			while (!haveToken) {
				try {
					notifyAll();
					logger.debug("Waiting : " + call + ", " + Thread.currentThread().hashCode());
					wait(100);
					haveToken = lockManager.haveToken();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
			logger.debug("Proceeding : " + call + ", " + Thread.currentThread().hashCode());
			call.proceed();
			return Boolean.TRUE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public int getOrder() {
		return 0;
	}

	public void setLockManager(ILockManager lockManager) {
		this.lockManager = lockManager;
	}

}
