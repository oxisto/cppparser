// forward declaration on global
/*class A;
class A* a;

class B {
    // forward declaration within a record
    class C* c;
};

// already using declared class
class C* c;*/

void function(
    // forward declaration within function parameter, should still count for current namespace
    /*class D* param1,
    // already using declared class
    class D* param2*/) {
    class D* d1;
    class D* d2;
}

// already using declared class
/*class D* d;

namespace Test {
    // forward declares Test::A
    class A;
}
*/
