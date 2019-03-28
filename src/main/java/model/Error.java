package model;

import java.util.logging.Level;

/**
 * Situations that prevent the execution of Main.
 */
public enum Error {

    // Free:  10, 12, 14, 19, 21
    NO_ARGUMENTS(1, "Main was run without arguments and there is no configuration file in the same directory."),
    NO_INPUT(2, "No input was specified with a command line argument nor in the configuration file."),
    COULD_NOT_WRITE_TO_OUTPUT_FILES(3, "There was an error creating/writing to the output files."),
    COULD_NOT_CONNECT_TO_NEO4j(4, " Unable to connect to Neo4j, ensure the database is running and that there is a working network connection to it."),
    COMMAND_LINE_ARGUMENTS_PARSING_ERROR(5, "There was an error parsing the command line arguments."),
    INVALID_INPUT_TYPE(6, "The input type is invalid."),
    COULD_NOT_READ_INPUT_FILE(7, "There was an error reading the input file."),
    MISSING_ARGUMENT(8, "There is an argument missing to process the input."),
    INPUT_PARSING_ERROR(9, "There was an error processing the input."),
    ERROR_INITIALIZING_PEPTIDE_MAPPER(13,"There was an error initializing peptide mapper. Make sure the directories are writable."),
    ERROR_READING_VEP_TABLES(14,"There was a problem reading the vepTable for chromosomes."),
    INPUT_FILE_EMPTY(16, "Input file is empty."),
    INVALID_MATCHING_TYPE(17, "The selected matching type is invalid."),
    ERROR_WITH_OUTPUT_FILE(18, "There was a problem writing to the output file."),
    VEP_DIRECTORY_NOT_FOUND(19, "The directory for the VEP tables was not found."),
    COULD_NOT_CREATE_SNP_TO_SWISSPROT_FILE(21, "Could not create or write to snp_to_swissprot file."),
    COULD_NOT_READ_FASTA_FILE(22, "Could not read the fasta file.");

    private final int code;
    private final String message;

    Error(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Error " + code + ": " + message;
    }

    public static void sendError(Error error){
        System.out.println(Level.SEVERE + ": " + error.getMessage());
        System.exit(error.getCode());
    }

    public static void sendError(Error error, int num){
    	System.out.println(Level.SEVERE + ": " + error.getMessage() + " " + num);
        System.exit(error.getCode());
    }
}
