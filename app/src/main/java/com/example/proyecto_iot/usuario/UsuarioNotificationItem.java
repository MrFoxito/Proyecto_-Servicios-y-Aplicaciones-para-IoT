package com.example.proyecto_iot.usuario;

public class UsuarioNotificationItem {

    public static final int TYPE_APPROVAL = 1;
    public static final int TYPE_VISIT    = 2;
    public static final int ACTION_PAYMENT     = 1;
    public static final int ACTION_APPOINTMENT = 2;

    private final int    type;
    private final String time;
    private final String title;
    private final String body;
    private final String ctaLabel;
    private final int    actionType;

    // Datos extra para abrir el detalle de cita real
    private String citaTitle;
    private String citaStatus;
    private String citaDate;
    private String citaAdvisor;
    private String citaLocation;
    private String citaNote;
    private boolean citaConfirmed;
    private String tramiteTitle;
    private String tramiteId;
    private String tramiteStatus;
    private String tramiteNote;
    private String tramiteDue;
    private boolean tramiteCanPay;

    public UsuarioNotificationItem(int type, String time, String title,
                                    String body, String ctaLabel, int actionType) {
        this.type       = type;
        this.time       = time;
        this.title      = title;
        this.body       = body;
        this.ctaLabel   = ctaLabel;
        this.actionType = actionType;
    }

    // Setters para datos de cita
    public void setCitaData(String citaTitle, String citaStatus, String citaDate,
                             String citaAdvisor, String citaLocation, String citaNote,
                             boolean citaConfirmed) {
        this.citaTitle     = citaTitle;
        this.citaStatus    = citaStatus;
        this.citaDate      = citaDate;
        this.citaAdvisor   = citaAdvisor;
        this.citaLocation  = citaLocation;
        this.citaNote      = citaNote;
        this.citaConfirmed = citaConfirmed;
    }

    public boolean hasCitaData() {
        return citaTitle != null && !citaTitle.isEmpty();
    }

    public void setTramiteData(String tramiteTitle, String tramiteId, String tramiteStatus,
                               String tramiteNote, String tramiteDue, boolean tramiteCanPay) {
        this.tramiteTitle = tramiteTitle;
        this.tramiteId = tramiteId;
        this.tramiteStatus = tramiteStatus;
        this.tramiteNote = tramiteNote;
        this.tramiteDue = tramiteDue;
        this.tramiteCanPay = tramiteCanPay;
    }

    public boolean hasTramiteData() {
        return tramiteTitle != null && !tramiteTitle.isEmpty();
    }

    public int    getType()      { return type; }
    public String getTime()      { return time; }
    public String getTitle()     { return title; }
    public String getBody()      { return body; }
    public String getCtaLabel()  { return ctaLabel; }
    public int    getActionType(){ return actionType; }

    public String  getCitaTitle()    { return citaTitle; }
    public String  getCitaStatus()   { return citaStatus; }
    public String  getCitaDate()     { return citaDate; }
    public String  getCitaAdvisor()  { return citaAdvisor; }
    public String  getCitaLocation() { return citaLocation; }
    public String  getCitaNote()     { return citaNote; }
    public boolean isCitaConfirmed() { return citaConfirmed; }
    public String  getTramiteTitle() { return tramiteTitle; }
    public String  getTramiteId()    { return tramiteId; }
    public String  getTramiteStatus(){ return tramiteStatus; }
    public String  getTramiteNote()  { return tramiteNote; }
    public String  getTramiteDue()   { return tramiteDue; }
    public boolean canPayTramite()   { return tramiteCanPay; }
}
