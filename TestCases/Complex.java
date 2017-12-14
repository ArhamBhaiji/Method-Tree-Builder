public class Complex{
	public void methodA() {
		methodB();
		methodB();
	}
	
	public void methodA(int s) {
		System.out.println("");
		methodA();
	}

	public void methodB() {

		methodA();
		methodA();

		for(int i = 0; i < 10; i++)
		methodC();
	
		m2(2);

		methodA(5);
	}

	public void methodC() {
		m3();
		m3();
	}

	public void m2 (int s) {
		methodB();
		Test t1 = new Test();
		t1.m3();
		m2(2);
	}

	public void m3 () {}

}
