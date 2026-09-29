public class Convertidor {

    private Moneda[] monedas = {
        new Moneda("Peso mexicano",    "MXN", 1.00),
        new Moneda("Dolar",            "USD", 0.057),
        new Moneda("Euro",             "EUR", 0.050),
        new Moneda("Libra esterlina",  "GBP", 0.043),
        new Moneda("Yen japones",      "JPY", 9.00),
        new Moneda("Dolar canadiense", "CAD", 0.080),
        new Moneda("Peso colombiano",  "COP", 189.46),
        new Moneda("Peso argentino",   "ARS", 86.25),
        new Moneda("Real brasileno",   "BRL", 0.29),
        new Moneda("Yuan chino",       "CNY", 0.38)
    };

    public Moneda[] getMonedas() {
        return monedas;
    }

    // Busca una moneda por su código (MXN, USD...). Devuelve null si no existe
    public Moneda buscar(String codigo) {
        if (codigo == null) return null;
        for (Moneda m : monedas) {
            if (m.getCodigo().equalsIgnoreCase(codigo)) {
                return m;
            }
        }
        return null;
    }

    // Pasa el monto a dólares y luego a la moneda destino
    public double convertir(double monto, Moneda origen, Moneda destino) {
        double enDolares = monto / origen.getTasa();
        return enDolares * destino.getTasa();
    }
}