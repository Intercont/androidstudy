package br.com.intercont.sunshine.app.data;

import android.test.AndroidTestCase;

/**
 * Classe de demonstra��o de cria��o de testes unit�rios para o Android
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
     * Teste unit�rio que demonstra o uso dos Asserts
     * Para cada teste unit�rio que crio, devo inicializar com testNomeDoTestesUnitario()
     * Colocando test no inicio do nome do m�todo
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
        tearDown roda sempre AP�S os testes
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
