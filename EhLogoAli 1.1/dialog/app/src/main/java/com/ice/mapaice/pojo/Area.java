package com.ice.mapaice.pojo;
/**
 *
 * @author claudio e joao paulo
 */
//classe para manipular os dados da �rea
//import javax.xml.bind.annotation.XmlRootElement;

//@XmlRootElement

public class Area{
    private Integer idarea;
    private String nome;
    private String responsavel;
    private String cidade;
    private String latitude;
    private String longitude;
    private String latitudeCid;
    private String longitudeCid;

    public Area() {
    }

    public Area(Integer idarea) {
        this.idarea = idarea;
    }

    public Area(Integer idarea, String nome, String responsavel, String cidade) {
        this.idarea = idarea;
        this.nome = nome;
        this.responsavel = responsavel;
        this.cidade = cidade;
    }

    public Area(Integer idarea, String nome, String responsavel, String cidade, String latitude, String longitude, String latitudeCid, String longitudeCid) {
        this.idarea = idarea;
        this.nome = nome;
        this.responsavel = responsavel;
        this.cidade = cidade;
        this.latitude = latitude;
        this.longitude = longitude;
        this.latitudeCid = latitudeCid;
        this.longitudeCid = longitudeCid;
    }

    public Integer getIdarea() {
        return idarea;
    }

    public void setIdarea(Integer idarea) {
        this.idarea = idarea;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
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

    public String getLatitudeCid() {
        return latitudeCid;
    }

    public void setLatitudeCid(String latitudeCid) {
        this.latitudeCid = latitudeCid;
    }

    public String getLongitudeCid() {
        return longitudeCid;
    }

    public void setLongitudeCid(String longitudeCid) {
        this.longitudeCid = longitudeCid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idarea != null ? idarea.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Area)) {
            return false;
        }
        Area other = (Area) object;
        if ((this.idarea == null && other.idarea != null) || (this.idarea != null && !this.idarea.equals(other.idarea))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "POJO.Area[ idarea=" + idarea + " ]";
    }

}
