package eu.europa.ec.rqesui.domain.extension

fun String.localizationFormatWithArgs(args: List<String> = emptyList()): String =
    args.fold(this) { acc, arg ->
        acc.replaceFirst("%@", arg)
    }