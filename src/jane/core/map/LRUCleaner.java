package jane.core.map;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import jane.core.Log;

final class LRUCleaner
{
	private static final class Singleton
	{
		private static final LRUCleaner instance = new LRUCleaner();
	}

	interface Cleanable
	{
		void sweep();

		void sweep(int newLowerSize, int newAcceptSize);
	}

	private final ExecutorService cleanerThread;

	private LRUCleaner()
	{
		cleanerThread = Executors.newSingleThreadExecutor(r ->
		{
			Thread t = new Thread(r, "LRUCleanerThread");
			t.setDaemon(true);
			t.setPriority(Thread.NORM_PRIORITY + 1);
			return t;
		});
	}

	static void submit(AtomicInteger status, Cleanable c)
	{
		if(!status.compareAndSet(0, 1)) return;
		Singleton.instance.cleanerThread.submit(() ->
		{
			try
			{
				c.sweep();
			}
			catch(Throwable e)
			{
				Log.error("LRUCleaner fatal exception:", e);
			}
			finally
			{
				status.set(0);
			}
		});
	}
}
