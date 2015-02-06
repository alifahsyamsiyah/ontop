package it.unibz.krdb.sql.api;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.sql.Reference;

import java.io.Serializable;
import java.sql.Types;

public class Attribute implements Serializable{
	
	private static final long serialVersionUID = -5780621780592347583L;
	
	/** Fields */
	private String name;
	private int type;
	private boolean bPrimaryKey;
	private Reference foreignKey;
	private int canNull;

	private String typeName;

    private boolean unique;



    public String toString() {
		return name + ":" + type;
	}
	
	public Attribute(String name) {
		this(name, 0, false, null, 0);
	}

	public Attribute(String name, int type) {
		this(name, type, false, null, 0);
	}
	
	/**
	 * Use Attribute(String name, int type, boolean primaryKey, Reference foreignKey) instead.
	 */
	@Deprecated
	public Attribute(String name, int type, boolean primaryKey, boolean foreignKey) {
		this(name, type, primaryKey, null, 0);
	}
	
	public Attribute(String name, int type, boolean primaryKey, Reference foreignKey) {
		this(name, type, primaryKey, foreignKey, 0);
	}

	/**
	 * Use Attribute(String name, int type, boolean primaryKey, Reference foreignKey, int canNull) instead.
	 */
	@Deprecated
	public Attribute(String name, int type, boolean primaryKey, boolean foreignKey, int canNull) {
		this(name, type, primaryKey, null, 0);
	}
	
	public Attribute(String name, int type, boolean primaryKey, Reference foreignKey, int canNull) {
		this(name,type,primaryKey,foreignKey,canNull,null);		
	}

    public Attribute(String name, int type, boolean primaryKey, Reference foreignKey, int canNull, String typeName) {
        this(name,type,primaryKey,foreignKey,canNull,typeName, false);
    }
	
	public Attribute(String name, int type, boolean primaryKey, Reference foreignKey, int canNull, String typeName, boolean unique) {
		this.name = name;
		this.type = type;
		this.bPrimaryKey = primaryKey;
		this.foreignKey = foreignKey;
		this.canNull = canNull;
		this.typeName = typeName;
        this.unique = unique;
	}
	
	public String getName() {
		return name;
	}
	
	public int getType() {
		return type;
	}
	
	public boolean isPrimaryKey() {
		return bPrimaryKey;
	}
	
	public boolean isForeignKey() {
		return foreignKey != null;
	}
	
	public boolean canNull() {
		return canNull == 1;
	}
	
	public Reference getReference() {
		return foreignKey;
	}
	
	/**
	 * Determines whether this attribute object contains a
	 * specified name.
	 * 
	 * @param name
	 * 			The name in question.
	 * @return Returns true if the attribute has the name,
	 * or false, otherwise.
	 */
	public boolean hasName(String name) {
		return (this.name.equals(name)) ? true : false;
	}

	/***
	 * Returns the name of the SQL type asssociated with this attribute. Note, the name maybe not match
	 * the integer SQL id. The integer SQL id comes from the {@link Types} class, and these are few. Often
	 * databases match extra datatypes they may provide to the same ID, e.g., in MySQL YEAR (which doesn't
	 * exists in standard SQL, is mapped to 91, the ID of DATE. This field helps in dissambiguating this 
	 * cases.
	 * 
	 * @return
	 */
	public String getSQLTypeName() {
		return typeName;
	}

	public void setSQLTypeName(String typeName) {
		this.typeName = typeName;
	}

    public boolean isUnique() {
        return unique;
    }
}
