/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package fish.focus.uvms.config.service.entity;

import static fish.focus.uvms.config.service.entity.Parameter.FIND_BY_ID;
import static fish.focus.uvms.config.service.entity.Parameter.LIST_ALL;
import static fish.focus.uvms.config.service.entity.Parameter.LIST_ALL_BY_IDS;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * The persistent class for the parameter database table.
 */
@Entity
@NamedQueries({
    @NamedQuery(name = FIND_BY_ID, query = "SELECT p FROM Parameter p WHERE p.id = :id"),
    @NamedQuery(name = LIST_ALL_BY_IDS, query = "SELECT p FROM Parameter p WHERE p.id IN :ids"),
    @NamedQuery(name = LIST_ALL, query = "SELECT p FROM Parameter p")
})
public class Parameter implements Serializable {

    public static final String FIND_BY_ID = "Parameter.findByName";
    public static final String LIST_ALL = "Parameter.listAll";
    public static final String LIST_ALL_BY_IDS = "Paramater.listAllByIds";

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "param_id")
    private String id;

    @Column(name = "param_value")
    private String value;

    @Column(name = "param_description")
    private String description;

    public String getParamId() {
        return this.id;
    }

    public void setParamId(String paramId) {
        this.id = paramId;
    }

    public String getParamValue() {
        return this.value;
    }

    public void setParamValue(String value) {
        this.value = value;
    }

    public String getParamDescription() {
        return this.description;
    }

    public void setParamDescription(String description) {
        this.description = description;
    }

}