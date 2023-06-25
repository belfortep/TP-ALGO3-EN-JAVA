import java.util.*;

public class App {
    public static void main(String[] args) {
        CuentaCorriente cuenta = new CuentaCorriente();
        Deposit.registrarTransaccionDe(150, cuenta);
        Withdraw.registrarTransaccionDe(50, cuenta);

        CuentaCorriente cuenta2 = new CuentaCorriente();
        Deposit.registrarTransaccionDe(200, cuenta2);
        Withdraw.registrarTransaccionDe(50, cuenta2);

        System.out.println(cuenta.balance());

        Portfolio portfolio = new Portfolio();
        Portfolio portfolio2 = new Portfolio();
        portfolio.register(cuenta);
        portfolio2.register(cuenta2);
        portfolio.register(portfolio2);

        System.out.println(portfolio.balance());

        Transferencia transferencia = Transferencia.hacerTransferencia(cuenta, cuenta2, 300);

        System.out.println(transferencia.pataDeDeposito().tuOtraPata());
        System.out.println(cuenta.balance());

    }
}

abstract class Transaccion {
    public abstract int resultadoCon(int balance);

    public abstract int value();
}

abstract class AccountTransaction extends Transaccion {
    public abstract int resultadoCon(int balance);

    public int value() {
        return valor;
    }

    public AccountTransaction(int valor) {
        this.valor = valor;
    }

    protected int valor;

    /*
     * public static Transaccion registrarTransaccionDe(int valor, Cuenta cuenta)
     * {
     * No puedo usar el this/self como en smalltalk para hacer la creacion dinamica,
     * cada subclase se tiene que encargar bastante zzzz
     * Transaccion transaccion = this.new();
     * 
     * 
     * return null;
     * }
     */
    // public abstract static Transaccion registrarTransaccionDe(int valor, Cuenta
    // cuenta); mira vos che, en java no se pueden crear abstract static
    /*
     * public static Transaccion registrarTransaccionDe(int valor, Cuenta cuenta)
     * {
     * return null;
     * }
     */
    
}

class Deposit extends AccountTransaction {

    public Deposit(int valor) {
        super(valor);
    }

    public int resultadoCon(int balance) {
        return balance + valor;
    }

    public static AccountTransaction registrarTransaccionDe(int valor, CuentaCorriente cuenta) {
        AccountTransaction transaccion = new Deposit(valor);
    
        cuenta.register(transaccion);

        return transaccion;
    }

}

class Withdraw extends AccountTransaction {
    public Withdraw(int valor) {
        super(valor);
    }

    public int resultadoCon(int balance) {
        return balance - valor;
    }

    public static AccountTransaction registrarTransaccionDe(int valor, CuentaCorriente cuenta) {
        AccountTransaction transaccion = new Withdraw(valor);

        cuenta.register(transaccion);

        return transaccion;
    }
}

abstract class Cuenta {
    public abstract int balance();

    public abstract boolean hasRegistered(Transaccion unaTransaccion);

    public abstract ArrayList<Transaccion> transactions();

}

class CuentaCorriente extends Cuenta {

    public CuentaCorriente() {
        transacciones = new ArrayList<Transaccion>();
    }

    public int balance() {
        final int balance[] = { 0 };
        // re truchardo, tengo que hacer esto para poder actualizar el valor dentro de la funcion :(
        transacciones.forEach((transaccion) -> {
            balance[0] = transaccion.resultadoCon(balance[0]);
        });

        return balance[0];
    }

    public boolean hasRegistered(Transaccion unaTransaccion) {
        return transacciones.contains(unaTransaccion);
    }

    public void register(Transaccion unaTransaccion) {
        transacciones.add(unaTransaccion);
    }

    public ArrayList<Transaccion> transactions() {
        return transacciones;
    }

    private ArrayList<Transaccion> transacciones;
}

class Portfolio extends Cuenta {

    public Portfolio() {
        cuentas = new ArrayList<Cuenta>();
    }

    @Override
    public int balance() {

        final int balance[] = { 0 };

        cuentas.forEach((cuenta) -> {
            balance[0] = balance[0] + cuenta.balance();
        });

        return balance[0];
    }

    @Override
    public boolean hasRegistered(Transaccion unaTransaccion) {

        final boolean encontrado[] = { false };

        cuentas.forEach((cuenta) -> {
            if (cuenta.hasRegistered(unaTransaccion)) {
                encontrado[0] = true;
            }
        });

        return encontrado[0];

    }

    @Override
    public ArrayList<Transaccion> transactions() {

        ArrayList<Transaccion> transacciones = new ArrayList<Transaccion>();

        cuentas.forEach((cuenta) -> {
            transacciones.addAll(cuenta.transactions());
        });

        return transacciones;
    }

    public void register(Cuenta unaCuenta) {
        cuentas.add(unaCuenta);
    }

    private ArrayList<Cuenta> cuentas;
}

abstract class PataDeTransferencia extends Transaccion {

    public abstract int resultadoCon(int balance);

    public abstract PataDeTransferencia tuOtraPata();

    public PataDeTransferencia(Transferencia transferencia, CuentaCorriente cuenta) {
        transferenciaAsociada = transferencia;

        cuenta.register(this);
    }

    @Override
    public int value() {

        return transferenciaAsociada.montoTransferido();
    }

    protected Transferencia transferenciaAsociada;

}

class PataDeDeposito extends PataDeTransferencia {

    public PataDeDeposito(Transferencia transferencia, CuentaCorriente cuenta) {
        super(transferencia, cuenta);
    }

    @Override
    public PataDeTransferencia tuOtraPata() {
        return transferenciaAsociada.pataDeExtraccion();
    }

    @Override
    public int resultadoCon(int balance) {

        return balance + this.value();
    }

}

class PataDeExtraccion extends PataDeTransferencia {
    //estoy obligado a crear el mensaje aunque solo sea para llamar al super, da error si no lo hago :(
    public PataDeExtraccion(Transferencia transferencia, CuentaCorriente cuenta) {
        super(transferencia, cuenta);
    }

    @Override
    public int resultadoCon(int balance) {
        return balance - this.value();
    }

    @Override
    public PataDeTransferencia tuOtraPata() {
        return transferenciaAsociada.pataDeDeposito();
    }

}

class Transferencia {

    public static Transferencia hacerTransferencia(CuentaCorriente cuentaDelDeposito, CuentaCorriente cuentaDeLaExtraccion,
        int cantidadDeDinero) {

            Transferencia transferencia = new Transferencia(cuentaDelDeposito, cuentaDeLaExtraccion, cantidadDeDinero);

            return transferencia;

        }

    private Transferencia(CuentaCorriente cuentaDelDeposito, CuentaCorriente cuentaDeLaExtraccion,
        int cantidadDeDinero) {
        montoTransferido = cantidadDeDinero;
        pataDeExtraccion = new PataDeExtraccion(this, cuentaDeLaExtraccion);
        pataDeDeposito = new PataDeDeposito(this, cuentaDelDeposito);
    }

    public int montoTransferido() {
        return montoTransferido;
    }

    public PataDeDeposito pataDeDeposito() {
        return pataDeDeposito;
    }

    public PataDeExtraccion pataDeExtraccion() {
        return pataDeExtraccion;
    }

    private int montoTransferido;
    private PataDeExtraccion pataDeExtraccion;
    private PataDeDeposito pataDeDeposito;

}