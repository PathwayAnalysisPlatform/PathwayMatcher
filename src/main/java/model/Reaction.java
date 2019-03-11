package model;

import com.google.common.collect.HashMultimap;

import java.io.Serializable;

/**
 * Represents chemical reactions or "reaction-like events" as in the Reactome data model.
 */
public class Reaction implements Comparable<Reaction>, Serializable {

    private String stId;

    public String getStId() {
        return stId;
    }

    public void setStId(String stId) {
        this.stId = stId;
    }

    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Protein proteinParticipantsWithRole with their role: input(reactant), output(product), catalyst, regulator
     * A protein can have multiple roles in the same reaction
     */
    private HashMultimap<String, Role> proteinParticipantsWithRole;

    public HashMultimap<String, Role> getProteinParticipantsWithRole() {
        return proteinParticipantsWithRole;
    }

    /**
     * Proteoform proteinParticipantsWithRole with their role: input(reactant), output(product), catalyst, regulator
     * A protein can have multiple roles in the same reaction
     */
    private HashMultimap<Proteoform, Role> proteoformParticipants;

    public HashMultimap<Proteoform, Role> getProteoformParticipants() {
        return proteoformParticipants;
    }

    public Reaction(String stId, String displayName) {
        this.stId = stId;
        this.displayName = displayName;
        proteinParticipantsWithRole = HashMultimap.create();
        proteoformParticipants = HashMultimap.create();
    }

    @Override
    public String toString() {
        return this.stId + "\t" + this.displayName;
    }

    public String toString(String separator) {
        return this.stId + separator + this.displayName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || !(obj instanceof Reaction)) return false;

        Reaction that = (Reaction) obj;

        return this.stId.equals(that.stId) && this.displayName.equals(that.displayName);
    }

    @Override
    public int compareTo(Reaction that) {
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

    public void addParticipant(String proteinAccession, Role role) {
        this.proteinParticipantsWithRole.put(proteinAccession, role);
    }

    public void addParticipant(Proteoform proteoform, Role role) {
        this.proteoformParticipants.put(proteoform, role);
    }
}
