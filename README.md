This implementation is based on a top-down red-black tree described in Data Structures and Algorithm Analysis in Java by Mark Allen Weiss.

Top-down remove implementation:
The algorithm assumes that current amd sibling are black and that parent is red.
By coloring the root sentinel node (header) red, we make sure that the base case
is true for the root, since a NIL node is considered to be black.

There are two main cases and a number of subcases that the algorithm handles:

Case 1: Current is double black, meaning it's children are  black or NIL.
Subcase 1A: Sibling is also double black, color flip current, sibling and parent. 
Subcase 1B: Sibling has a red outer child -> promote the red child, making it the grandparent of current and flip colors of relevant nodes.
Subcase 1C: Sibling has a red inner child -> same action as 1B but performs a double rotation.

Case 2: One of current's children are red.
Subcase 2A: The "Lucky subcase", the red child is in the traversal path, no action needed.
Subcase 2B: The red child is not in the traversal path -> promote the red child and flip colors.
