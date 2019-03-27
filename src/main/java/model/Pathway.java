/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class Pathway implements Comparable<Pathway>, Serializable {

    // Inherent attributes of a pathway
    // These are filled in the Search stage
    private String stId;
    private String displayName;
    private TreeSet<Pathway> topLevelPathwaySet;
    private int numEntitiesTotal;
    private int numEntitiesTotalByProteoform;
    private int numReactionsTotal;
    
    // Attributes for the analysis
    // These are filled in the Analysis stage
    private double entitiesRatio;
    private double pValue;
    private double entitiesFDR;
    private Set<Proteoform> entitiesFound;
    
    private double reactionsRatio;
    private Set<String> reactionsFound;

    /**
     * Create a new instance of a top level pathway. Sets the topLevelPathway to itself.
     *
     * @param id
     * @param name
     */
    public Pathway(String id, String name) {
        this.stId = id;
        this.displayName = name;
        topLevelPathwaySet = new TreeSet<>();
        entitiesFound = new TreeSet<>();
        reactionsFound = new TreeSet<>();
        pValue = 1;
        entitiesFDR = 1;
    }

    public String getStId() {
        return stId;
    }

    public void setStId(String stId) {
        this.stId = stId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public TreeSet<Pathway> getTopLevelPathwaySet() {
        return topLevelPathwaySet;
    }

    public void setTopLevelPathwaySet(TreeSet<Pathway> topLevelPathway) {
        this.topLevelPathwaySet = topLevelPathway;
    }

    public int getNumEntitiesTotal() {
        return numEntitiesTotal;
    }

    public void setNumEntitiesTotal(int numEntitiesTotal) {
        this.numEntitiesTotal = numEntitiesTotal;
    }

    public double getEntitiesRatio() {
        return entitiesRatio;
    }

    public void setEntitiesRatio(double entitiesRatio) {
        this.entitiesRatio = entitiesRatio;
    }

    public double getPValue() {
        return pValue;
    }

    public void setpValue(double pValue) {
        this.pValue = pValue;
    }

    public double getEntitiesFDR() {
        return entitiesFDR;
    }

    public void setEntitiesFDR(double entitiesFDR) {
        this.entitiesFDR = entitiesFDR;
    }

    public Set<Proteoform> getEntitiesFound() {
        return entitiesFound;
    }

    public void setEntitiesFound(Set<Proteoform> entitiesFound) {
        this.entitiesFound = entitiesFound;
    }

    public int getNumReactionsTotal() {
        return numReactionsTotal;
    }

    public void setNumReactionsTotal(int numReactionsTotal) {
        this.numReactionsTotal = numReactionsTotal;
    }

    public double getReactionsRatio() {
        return reactionsRatio;
    }

    public void setReactionsRatio(double reactionsRatio) {
        this.reactionsRatio = reactionsRatio;
    }

    public Set<String> getReactionsFound() {
        return reactionsFound;
    }

    public String getReactionsFoundString() {
        StringBuilder str = new StringBuilder();
        boolean first = true;
        for (String reaction : reactionsFound) {
            if (!first) {
                str.append(",");
            }
            str.append(reaction);
            first = false;
        }

        return str.toString();
    }

    public void setReactionsFound(Set<String> reactionsFound) {
        this.reactionsFound = reactionsFound;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || !(obj instanceof Pathway)) return false;

        Pathway that = (Pathway) obj;

        return this.stId.equals(that.stId) && this.displayName.equals(that.displayName);
    }

    @Override
    public String toString() {
        return this.stId + "," + this.displayName;
    }

    @Override
    public int compareTo(Pathway that) {

        if (this.equals(that)) return 0;

        // First by displayName
        if (!this.displayName.equals(that.displayName)) {
            return this.displayName.compareTo(that.displayName);
        }

        // Second by stId
        if (!this.stId.equals(that.stId)) {
            return this.stId.compareTo(that.stId);
        }

        assert this.equals(that) : "Check consistency with equals";

        return 0;
    }
    
    public String getEntitiesFoundString(InputType inputType) {
		StringBuilder str = new StringBuilder();
		boolean first = true;

		for (Proteoform proteoform : this.getEntitiesFound()) {
			if (!first) {
				str.append(",");
			}

			switch (inputType) {
			case PROTEOFORM:
				str.append("\"" + ProteoformFormat.SIMPLE.getString(proteoform) + "\"");
			default:
				str.append(proteoform.getUniProtAccWithIsoform());
			}

			first = false;
		}
		return str.toString();
	}

	public int getNumEntitiesTotalByProteoform() {
		return numEntitiesTotalByProteoform;
	}

	public void setNumEntitiesTotalByProteoform(int numEntitiesTotalByProteoform) {
		this.numEntitiesTotalByProteoform = numEntitiesTotalByProteoform;
	}
}
