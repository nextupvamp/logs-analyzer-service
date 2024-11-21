package backend.academy.data;

import com.beust.jcommander.Parameter;
import java.util.List;
import lombok.Getter;

@Getter
public class Args {
    private static final String PATH_ARG = "--path";
    private static final String FROM_ARG = "--from";
    private static final String TO_ARG = "--to";
    private static final String FORMAT_ARG = "--format";
    private static final String FILTER_FIELD_ARG = "--filter-field";
    private static final String FILTER_VALUE_ARG = "--filter-value";
    private static final String REPORT_FILE_NAME = "--report-file-name";

    @Parameter(names = PATH_ARG, variableArity = true, required = true)
    private List<String> paths;

    @Parameter(names = FROM_ARG, arity = 1)
    private String from;

    @Parameter(names = TO_ARG, arity = 1)
    private String to;

    @Parameter(names = FORMAT_ARG, arity = 1)
    private String format;

    @Parameter(names = FILTER_FIELD_ARG, arity = 1)
    private String filterField;

    @Parameter(names = FILTER_VALUE_ARG, arity = 1)
    private String filterValue;

    @Parameter(names = REPORT_FILE_NAME, arity = 1)
    private String reportFileName;
}
