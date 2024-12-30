package ru.nextupvamp.data;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

public record PathsData(
    List<URI> uris,
    List<Path> paths
) {
}
