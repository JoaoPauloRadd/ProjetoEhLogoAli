package com.ice.toaqui.pojo;

/**
 *
 * @author claudio e joão
 */
//classe para manipular os dados da ?rea
//import javax.xml.bind.annotation.XmlRootElement;

//@XmlRootElement

public class Area{
    private Integer idarea;
    private String nome;
    private String responsavel;
    private String cidade;

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
