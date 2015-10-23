package com.ice.mapaice.pojo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by USER on 03/09/2015.
 */
public class Cidade {
    private Integer idCidade;
    private String nome;
    private String responsavel;
    private List<Area> areas;

    public Cidade() {
    }

    public Cidade(Integer idCidade) {
        this.idCidade = idCidade;
    }

    public Cidade(Integer idCidade, String nome, String responsavel) {
        this.idCidade = idCidade;
        this.nome = nome;
        this.responsavel = responsavel;
    }

    public Cidade(Integer idCidade, String nome) {
        this.idCidade = idCidade;
        this.nome = nome;
        this.responsavel = null;
    }

    public Cidade(Integer idCidade, String nome, String responsavel, List<Area> areas) {
        this.idCidade = idCidade;
        this.nome = nome;
        this.responsavel = responsavel;
        this.areas = areas;
    }

    public Cidade(Integer idCidade, String nome, List<Area> areas) {
        this.idCidade = idCidade;
        this.nome = nome;
        this.responsavel = null;
        this.areas = areas;
    }

    public Integer getIdCidade() {
        return idCidade;
    }

    public void setIdCidade(Integer idCidade) {
        this.idCidade = idCidade;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idCidade != null ? idCidade.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Cidade)) {
            return false;
        }
        Cidade other = (Cidade) object;
        if ((this.idCidade == null && other.idCidade != null) || (this.idCidade != null && !this.idCidade.equals(other.idCidade))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "POJO.Cidade[ idCidade=" + idCidade + " ]";
    }

    public List<Area> getAreas(){
        return this.areas;
    }

    public void setAreas(List<Area> areas){
        this.areas = new ArrayList<Area>(areas);
    }

    public void updateAreas(List<Area> areas){

        Area toDelete = new Area();

        for (Area temp : areas) {

            Iterator<Area> it = this.areas.iterator();
            while (it.hasNext()){
                Area aux = it.next();
                if(aux.getIdarea().equals(temp.getIdarea())){
                    toDelete = new Area(aux.getIdarea(), aux.getNome(), aux.getResponsavel(), aux.getCidade());
                }
            }
            if(toDelete.getIdarea()!=null) {
                this.areas.remove(toDelete);
            }
        }
        this.areas.addAll(areas);
    }
}
