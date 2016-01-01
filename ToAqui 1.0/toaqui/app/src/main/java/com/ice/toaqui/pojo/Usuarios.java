package com.ice.toaqui.pojo;

/**
 * Created by USER on 22/09/2015.
 */
public class Usuarios {
    private String email;


    public Usuarios() {
    }

    public Usuarios(String email) {
        this.email = email;
    }


    public String getEmail() {
        return email;
    }

    public void setemail(String nome) {
        this.email = email;
    }
    /*
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idarea != null ? idarea.hashCode() : 0);
        return hash;
    }
*/
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Area)) {
            return false;
        }
        Usuarios other = (Usuarios) object;
        if ((this.email == null && other.email != null) || (this.email != null && !this.email.equals(other.email))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "POJO.Area[ email=" + email + " ]";
    }
}
