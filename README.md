# jvm-obj-size

Sometimes it is necessary to estimate the size of Java object in JVM, which is helpful in the following cases:

* **Memory leak detection:** In some cases you may suspect some objects before and after memory leak, at this time you need to measure the size of these objects.

* **Cache implementation:** A cache need to detect how much memory each object occupies and throw away abnormal objects, like some extremely big objects.

## How to use

Please add VM arguments before your application.

```java
-javaagent:lib/obj-size-fetcher.jar
```

Notice that `lib/obj-size-fetcher.jar` is my jar file path, you should replace it with your jar file path.

## Examples

```java
/**
 * -XX:-UseCompressedOops
 * 
 * @author tinylcy
 *
 */
public class ObjSizeFetcherTest {

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
		long size = 0;
		long fullSize = 0;

		A a = new A();
		size = ObjSizeFetcher.sizeOf(a);
		fullSize = ObjSizeFetcher.fullSizeOf(a);
		msg("a", size, fullSize);
		// +UseCompressedOops: size = 16, fullSize = 16
		// -UseCompressedOops: size = 24, fullSize = 24

		B b = new B();
		size = ObjSizeFetcher.sizeOf(b);
		fullSize = ObjSizeFetcher.fullSizeOf(b);
		// +UseCompressedOops: size = 24, fullSize = 24
		// -UseCompressedOops: size = 24, fullSize = 24
		msg("b", size, fullSize);

		size = ObjSizeFetcher.sizeOf(new int[2]);
		fullSize = ObjSizeFetcher.fullSizeOf(new int[2]);
		msg("int[2]", size, fullSize);
		// +UseCompressedOops: size = 24, fullSize = 24
		// -UseCompressedOops: size = 32, fullSize = 32

		size = ObjSizeFetcher.sizeOf(new char[2]);
		fullSize = ObjSizeFetcher.fullSizeOf(new char[2]);
		msg("char[2]", size, fullSize);
		// +UseCompressedOops: size = 24, fullSize = 24
		// -UseCompressedOops: size = 32, fullSize = 32

		String s = new String("aaaaaaaa");
		size = ObjSizeFetcher.sizeOf(s);
		fullSize = ObjSizeFetcher.fullSizeOf(s);
		msg("s", size, fullSize);
		// +UseCompressedOops: size = 24, fullSize = 56
		// -UseCompressedOops: size = 32, fullSize = 72

		C c = new C(a, b);
		size = ObjSizeFetcher.sizeOf(c);
		fullSize = ObjSizeFetcher.fullSizeOf(c);
		msg("c", size, fullSize);
		// +UseCompressedOops: size = 24, fullSize = 64
		// -UseCompressedOops: size = 32, fullSize = 80

	}

	private static void msg(String obj, long size, long fullSize) {
		System.err.println(obj + "--> size = " + size + ", fullSize = " + fullSize);
	}

}

class A {
	int a;
}

class B {
	int a;
	int b;
}

class C {
	A a;
	B b;

	public C(A a, B b) {
		this.a = a;
		this.b = b;
	}
}
```