package org.mwinther;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Denna klass ska inte lämnas in, den läggs automatiskt till vid testning i ilearn
 */
class RedBlackTreeTest {
	private static final int BLACK = 1;
	private static final int RED = 0;

	@ParameterizedTest(name = "{index}: {0} values added or removed")
	@ValueSource(ints = { 5, 6, 7, 8, 9, 10, 15, 20, 25, 100, 1000 })
	void test(int tests) {
		RedBlackTree<Integer> tree = new RedBlackTree<>();

		Random rnd = new Random(tests); // seed ensures consistent test results
		SortedSet<Integer> oracle = new TreeSet<>();
		List<String> sequence = new ArrayList<>();

		for (int n = 0; n < tests; n++) {
			int value = rnd.nextInt(tests);
			if (rnd.nextDouble() < 0.66) {
				tree.insert(value);
				oracle.add(value);
				sequence.add("+" + value);
			} else {
				tree.remove(value);
				oracle.remove(value);
				sequence.add("-" + value);
			}

			assertIsRedBlackTree(tree);
			for (value = 0; value < tests; value++) {
				assertEquals(oracle.contains(value), tree.contains(value),
						"Wrong result from contains for value %d, sequence of operations was: %s".formatted(value,
								sequence.toString()));
			}
		}

		// Removing all elements in sorted order (since we use a sorted set) will force
		// the tree to change
		for (int value : oracle) {
			tree.remove(value);
			assertIsRedBlackTree(tree);
			assertFalse(tree.contains(value), "The value %d should just have been removed".formatted(value));
		}

		assertTrue(tree.isEmpty(), "The tree should be empty after removing all elements");

	}

	private void assertIsRedBlackTree(RedBlackTree<?> tree) {
		try {
			Object header = getHeader(tree);
			Object nullNode = getNullNode(tree);

			// The header node is always smaller than every element in the tree, so it can't
			// have a left child
			int expectedBlackNodes = countExpectedBlackNodes(getRight(header), nullNode);
			assertIsRedBlackTree(expectedBlackNodes, 0, BLACK, getRight(header), nullNode);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			fail(e);
		}
	}

	private void assertIsRedBlackTree(int expectedBlackNodes, int actualBlackNodes, int parentColor, Object node,
			Object nullNode)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if (node == nullNode) {
			assertEquals(expectedBlackNodes, actualBlackNodes, "Wrong number of black nodes in path");
		} else {
			switch (getColor(node)) {
			case BLACK:
				actualBlackNodes++;
				break;
			case RED:
				assertEquals(BLACK, parentColor, "A red node must have a black parent");
				break;
			default:
				fail("Unexpected color: " + getColor(node));
			}

			assertIsRedBlackTree(expectedBlackNodes, actualBlackNodes, getColor(node), getLeft(node), nullNode);
			assertIsRedBlackTree(expectedBlackNodes, actualBlackNodes, getColor(node), getRight(node), nullNode);
		}
	}

	private int countExpectedBlackNodes(Object root, Object nullNode)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		int count = 0;
		Object node = root;
		while (node != nullNode) {
			if (getColor(node) == BLACK)
				count++;
			node = getLeft(node);
		}
		return count;
	}

	private Object getHeader(RedBlackTree<?> tree)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = RedBlackTree.class.getDeclaredField("header");
		field.setAccessible(true);
		return field.get(tree);
	}

	private Object getNullNode(RedBlackTree<?> tree)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = RedBlackTree.class.getDeclaredField("nullNode");
		field.setAccessible(true);
		return field.get(tree);
	}

	private int getColor(Object node)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = node.getClass().getDeclaredField("color");
		field.setAccessible(true);
		return field.getInt(node);
	}

	private Object getLeft(Object node)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = node.getClass().getDeclaredField("left");
		field.setAccessible(true);
		return field.get(node);
	}

	private Object getRight(Object node)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = node.getClass().getDeclaredField("right");
		field.setAccessible(true);
		return field.get(node);
	}

}
