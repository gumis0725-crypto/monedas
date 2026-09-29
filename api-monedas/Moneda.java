public class Moneda {
    private String nombre;
    private String codigo;
    private double tasa; // cuánto vale 1 USD en esta moneda

    public Moneda(String nombre, String codigo, double tasa) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.tasa = tasa;
    }

    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }
    public double getTasa()   { return tasa; }
}