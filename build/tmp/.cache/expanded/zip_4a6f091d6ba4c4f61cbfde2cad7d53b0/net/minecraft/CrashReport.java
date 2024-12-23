package net.minecraft;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import net.minecraft.util.MemoryReserve;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

public class CrashReport {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
    private final String title;
    private final Throwable exception;
    private final List<CrashReportCategory> details = Lists.newArrayList();
    @Nullable
    private Path saveFile;
    private boolean trackingStackTrace = true;
    private StackTraceElement[] uncategorizedStackTrace = new StackTraceElement[0];
    private final SystemReport systemReport = new SystemReport();

    public CrashReport(String pTitle, Throwable pException) {
        this.title = pTitle;
        this.exception = pException;
    }

    public String getTitle() {
        return this.title;
    }

    public Throwable getException() {
        return this.exception;
    }

    public String getDetails() {
        StringBuilder stringbuilder = new StringBuilder();
        this.getDetails(stringbuilder);
        return stringbuilder.toString();
    }

    public void getDetails(StringBuilder pBuilder) {
        if ((this.uncategorizedStackTrace == null || this.uncategorizedStackTrace.length <= 0) && !this.details.isEmpty()) {
            this.uncategorizedStackTrace = ArrayUtils.subarray(this.details.get(0).getStacktrace(), 0, 1);
        }

        if (this.uncategorizedStackTrace != null && this.uncategorizedStackTrace.length > 0) {
            pBuilder.append("-- Head --\n");
            pBuilder.append("Thread: ").append(Thread.currentThread().getName()).append("\n");
            pBuilder.append(net.minecraftforge.logging.CrashReportAnalyser.appendSuspectedMods(this.exception, this.uncategorizedStackTrace));
            pBuilder.append("Stacktrace:");
            pBuilder.append(net.minecraftforge.logging.CrashReportExtender.generateEnhancedStackTrace(this.uncategorizedStackTrace));
        }

        for (CrashReportCategory crashreportcategory : this.details) {
            crashreportcategory.getDetails(pBuilder);
            pBuilder.append("\n\n");
        }

        net.minecraftforge.logging.CrashReportExtender.extendSystemReport(systemReport);
        this.systemReport.appendToCrashReportString(pBuilder);
    }

    public String getExceptionMessage() {
        StringWriter stringwriter = null;
        PrintWriter printwriter = null;
        Throwable throwable = this.exception;
        if (throwable.getMessage() == null) {
            if (throwable instanceof NullPointerException) {
                throwable = new NullPointerException(this.title);
            } else if (throwable instanceof StackOverflowError) {
                throwable = new StackOverflowError(this.title);
            } else if (throwable instanceof OutOfMemoryError) {
                throwable = new OutOfMemoryError(this.title);
            }

            throwable.setStackTrace(this.exception.getStackTrace());
        }

        return net.minecraftforge.logging.CrashReportExtender.generateEnhancedStackTrace(throwable);
    }

    public String getFriendlyReport(ReportType pType, List<String> pLinks) {
        StringBuilder stringbuilder = new StringBuilder();
        pType.appendHeader(stringbuilder, pLinks);
        stringbuilder.append("Time: ");
        stringbuilder.append(DATE_TIME_FORMATTER.format(ZonedDateTime.now()));
        stringbuilder.append("\n");
        stringbuilder.append("Description: ");
        stringbuilder.append(this.title);
        stringbuilder.append("\n\n");
        stringbuilder.append(this.getExceptionMessage());
        stringbuilder.append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");

        for (int i = 0; i < 87; i++) {
            stringbuilder.append("-");
        }

        stringbuilder.append("\n\n");
        this.getDetails(stringbuilder);
        return stringbuilder.toString();
    }

    public String getFriendlyReport(ReportType pType) {
        return this.getFriendlyReport(pType, List.of());
    }

    @Nullable
    public Path getSaveFile() {
        return this.saveFile;
    }

    public boolean saveToFile(Path pPath, ReportType pType, List<String> pLinks) {
        if (this.saveFile != null) {
            return false;
        } else {
            try {
                if (pPath.getParent() != null) {
                    FileUtil.createDirectoriesSafe(pPath.getParent());
                }

                try (Writer writer = Files.newBufferedWriter(pPath, StandardCharsets.UTF_8)) {
                    writer.write(this.getFriendlyReport(pType, pLinks));
                }

                this.saveFile = pPath;
                return true;
            } catch (Throwable throwable1) {
                LOGGER.error("Could not save crash report to {}", pPath, throwable1);
                return false;
            }
        }
    }

    public boolean saveToFile(Path pPath, ReportType pType) {
        return this.saveToFile(pPath, pType, List.of());
    }

    public SystemReport getSystemReport() {
        return this.systemReport;
    }

    public CrashReportCategory addCategory(String pName) {
        return this.addCategory(pName, 1);
    }

    public CrashReportCategory addCategory(String pCategoryName, int pStacktraceLength) {
        CrashReportCategory crashreportcategory = new CrashReportCategory(pCategoryName);
        if (this.trackingStackTrace) {
            int i = crashreportcategory.fillInStackTrace(pStacktraceLength);
            StackTraceElement[] astacktraceelement = this.exception.getStackTrace();
            StackTraceElement stacktraceelement = null;
            StackTraceElement stacktraceelement1 = null;
            int j = astacktraceelement.length - i;
            if (j < 0) {
                LOGGER.error("Negative index in crash report handler ({}/{})", astacktraceelement.length, i);
            }

            if (astacktraceelement != null && 0 <= j && j < astacktraceelement.length) {
                stacktraceelement = astacktraceelement[j];
                if (astacktraceelement.length + 1 - i < astacktraceelement.length) {
                    stacktraceelement1 = astacktraceelement[astacktraceelement.length + 1 - i];
                }
            }

            this.trackingStackTrace = crashreportcategory.validateStackTrace(stacktraceelement, stacktraceelement1);
            if (astacktraceelement != null && astacktraceelement.length >= i && 0 <= j && j < astacktraceelement.length) {
                this.uncategorizedStackTrace = new StackTraceElement[j];
                System.arraycopy(astacktraceelement, 0, this.uncategorizedStackTrace, 0, this.uncategorizedStackTrace.length);
            } else {
                this.trackingStackTrace = false;
            }
        }

        this.details.add(crashreportcategory);
        return crashreportcategory;
    }

    public static CrashReport forThrowable(Throwable pCause, String pDescription) {
        while (pCause instanceof CompletionException && pCause.getCause() != null) {
            pCause = pCause.getCause();
        }

        CrashReport crashreport;
        if (pCause instanceof ReportedException reportedexception) {
            crashreport = reportedexception.getReport();
        } else {
            crashreport = new CrashReport(pDescription, pCause);
        }

        return crashreport;
    }

    public static void preload() {
        MemoryReserve.allocate();
        new CrashReport("Don't panic!", new Throwable()).getFriendlyReport(ReportType.CRASH);
    }
}
