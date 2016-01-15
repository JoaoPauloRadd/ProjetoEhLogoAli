package com.ice.toaqui.pojo;
/**
 *
 * @author claudio & João Paulo Radd
 */
//dados para manipular os dados do local
public class Local{
    private Integer idlocal;
    private String nome;
    private String latitude;
    private String longitude;
    private Integer idarea;
    private String link;

    public Local() {
    }

    public Local(Integer idlocal) {
        this.idlocal = idlocal;
    }

    public Local(Integer idlocal, String nome, String latitude, String longitude,Integer idarea, String link) {
        this.idlocal = idlocal;
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
        this.idarea = idarea;
        this.link = link;
    }
    public Local(Integer idlocal, String nome, String latitude, String longitude,Integer idarea) {
        this.idlocal = idlocal;
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
        this.idarea = idarea;
        //this.link = link;
    }

    public Integer getIdlocal() {
        return idlocal;
    }

    public void setIdlocal(Integer idlocal) {
        this.idlocal = idlocal;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Integer getIdarea() {
        return idarea;
    }

    public void setIdarea(Integer idarea) {
        this.idarea = idarea;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idlocal != null ? idlocal.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Local)) {
            return false;
        }
        Local other = (Local) object;
        if ((this.idlocal == null && other.idlocal != null) || (this.idlocal != null && !this.idlocal.equals(other.idlocal))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "POJO.Local[ idlocal=" + idlocal + " ]";
    }

}
