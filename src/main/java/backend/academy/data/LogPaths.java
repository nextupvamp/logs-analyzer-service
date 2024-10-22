package backend.academy.data;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public record LogPaths(
    List<URL> urls,
    List<Path> paths
) {
}
