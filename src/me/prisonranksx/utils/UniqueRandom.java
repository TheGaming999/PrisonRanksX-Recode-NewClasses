package me.prisonranksx.utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generate random unique numbers. Using the method {@link #generate()} x times
 * won't generate the same number till the amount of generated numbers reaches
 * the limit. For example, {@link #generate()} was used 10 times. The generated
 * numbers in order are: [5, 4, 0, 1, 9, 6, 3, 7, 2, 8].
 * Notice that there wasn't any number that got generated twice. However, using
 * the method again can generate one of the numbers that got generated such as 4
 * since the limit was reached. The same outcome can be forced using
 * {@link #reset()}.
 */
public class UniqueRandom {

	private static final int DEFAULT_LIMIT = 10;
	private static final UniqueRandom GLOBAL = new UniqueRandom(DEFAULT_LIMIT);
	private int limit;
	private final List<Integer> uniqueList;

	public static synchronized UniqueRandom global() {
		return GLOBAL;
	}

	public UniqueRandom() {
		this(DEFAULT_LIMIT);
	}

	public UniqueRandom(int limit) {
		this.limit = limit;
		uniqueList = Collections.synchronizedList(new LinkedList<>());
		for (int j = 1; j <= limit; ++j) uniqueList.add(j);
	}

	public UniqueRandom(int limit, LinkedList<Integer> uniqueList) {
		this.limit = limit;
		this.uniqueList = uniqueList;
	}

	public void reset() {
		uniqueList.clear();
	}

	public boolean hasReachedLimit() {
		return uniqueList.isEmpty();
	}

	public int generateDefault() {
		if (!uniqueList.isEmpty()) return uniqueList.remove(0);
		for (int j = 1; j <= DEFAULT_LIMIT; ++j) uniqueList.add(j);
		Collections.shuffle(uniqueList);
		return uniqueList.remove(0);
	}

	/**
	 * Gets next int from shuffled list, shuffles list again if all numbers have
	 * been generated
	 *
	 * @return random unique int
	 */
	public int generate() {
		synchronized (uniqueList) {
			if (!uniqueList.isEmpty()) return uniqueList.remove(0);
			for (int j = 1; j <= limit; ++j) uniqueList.add(j);
			Collections.shuffle(uniqueList);
			return uniqueList.remove(0);
		}
	}

	public int generate(boolean async) {
		return async ? generateAsyncAndGet() : generate();
	}

	public CompletableFuture<Integer> generateAsync() {
		return CompletableFuture.supplyAsync(() -> {
			synchronized (uniqueList) {
				if (!uniqueList.isEmpty()) return uniqueList.remove(0);
				for (int j = 1; j <= limit; ++j) uniqueList.add(j);
				Collections.shuffle(uniqueList);
				return uniqueList.remove(0);
			}
		});
	}

	public int generateAsyncAndGet() {
		return generateAsync().join();
	}

	public int setRandomLimit(int limit) {
		return this.limit = limit;
	}

	public int getRandomLimit() {
		return limit;
	}
}
