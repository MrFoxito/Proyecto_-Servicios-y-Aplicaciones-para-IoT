package com.example.proyecto_iot.data;

public class AccountContext {
    public final String uid;
    public final String nombres;
    public final String apellidos;
    public final String nombreCompleto;
    public final String email;
    public final String telefono;
    public final String rol;
    public final String estado;
    public final String empresaId;
    public final String empresaNombre;
    public final String avatarUrl;

    public AccountContext(
            String uid,
            String nombres,
            String apellidos,
            String email,
            String telefono,
            String rol,
            String estado,
            String empresaId,
            String empresaNombre,
            String avatarUrl
    ) {
        this.uid = value(uid);
        this.nombres = value(nombres);
        this.apellidos = value(apellidos);
        this.nombreCompleto = (this.nombres + " " + this.apellidos).trim();
        this.email = value(email);
        this.telefono = value(telefono);
        this.rol = value(rol);
        this.estado = value(estado);
        this.empresaId = value(empresaId);
        this.empresaNombre = value(empresaNombre);
        this.avatarUrl = value(avatarUrl);
    }

    private static String value(String input) {
        return input == null ? "" : input.trim();
    }
}
