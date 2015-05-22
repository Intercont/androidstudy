package br.com.intercont.sunshine.app.data;

import android.test.AndroidTestCase;

/**
 * Classe de demonstração de criação de testes unitários para o Android
 */
public class TestPractice extends AndroidTestCase {
    /*
        setUp roda sempre ANTES dos testes
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Teste unitário que demonstra o uso dos Asserts
     * Para cada teste unitário que crio, devo inicializar com testNomeDoTestesUnitario()
     * Colocando test no inicio do nome do método
     * @throws Throwable
     */
    public void testThatDemonstratesAssertions() throws Throwable {
        int a = 5;
        int b = 3;
        int c = 5;
        int d = 10;

        assertEquals("X should be equal", a, c);
        assertTrue("Y should be true", d > a);
        assertFalse("Z should be false", a == b);

        if (b > d) {
            fail("XX should never happen");
        }
    }

    /*
        tearDown roda sempre APÓS os testes
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
