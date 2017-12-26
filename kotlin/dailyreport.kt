#!/usr/bin/env kotlin-script.sh
@file:Suppress("PackageDirectoryMismatch")

package scripts.dailyreport

import org.apache.commons.io.IOUtils
import org.zeroturnaround.exec.ProcessExecutor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Daily report generator
 * Required: Kotlin, Git, Bash
 */

val dir = "reports"

fun main(args: Array<String>) {
    val reportFile = createReportFile()

    reportFile.writeText("") // clear text if exist

    val templates = getTemplates()
    templates.map { template ->
        formatTemplate(template)
    }.forEach { prepared ->
        reportFile.appendText(prepared.name)
        reportFile.appendText(prepared.body)
        println("Generated report by template: ${prepared.name}")
    }

    println("Path to file: ${reportFile.absolutePath}")
}

fun createReportFile(): File {
    val month = SimpleDateFormat("MMMM").format(Date()).toLowerCase()
    val directory = createDirectory("$dir/$month/")
    val date = SimpleDateFormat("YYYY-MMMM-dd").format(Date())
    return createFile(directory, "$date.txt")
}

fun formatTemplate(template: Template): Template {
    val fieldFormatter = Field.DateFormatter()
    var formatter = SimpleDateFormat(fieldFormatter.value)
    findFieldValue(fieldFormatter, template.body) { value, lineForRemove ->
        template.body = template.body.replace(lineForRemove, "")
        formatter = SimpleDateFormat(value)
    }

    var body = ""
    getCommits(getGitName()).forEach {
        body += ">• $it\n"
    }
    val fieldCommit = Field.Commit(body)
    val fieldDate = Field.Date(formatter.format(Date()))
    val fieldProject = Field.Project(getProjectName())

    listOf(fieldCommit, fieldDate, fieldProject).forEach { field ->
        template.body = template.body.replace(field)
    }
    return Template(template.name, template.body)
}

fun getTemplates(): List<Template> {
    val folder = File("$dir/template")
    if (!folder.exists()) {
        folder.mkdirs()
    }

    val files = folder.listFiles()

    val templates = files.map { file ->
        val name = file.name
        val text = IOUtils.toString(file.inputStream())
        Template(name, text)
    }

    return templates
}

fun String.replace(field: Field): String {
    var text = this
    field.args.forEach { arg ->
        text = text.replace(arg, field.value)
    }
    return text
}

fun findFieldValue(field: Field, text: String, param: (String, String) -> Unit) {
    val lines = text.split("\n")
    lines.forEach { line ->
        field.args.forEach { arg ->
            val index = line.indexOf(arg, 0, false)

            if (index >= 0) {
                var stringWithField = line.substring(index + arg.length, line.length)
                stringWithField = stringWithField.replace("\"", "")
                param(stringWithField, line)
            }
        }
    }
}

fun getCommits(gitname: String): List<String> {
//    return exec { "git log --pretty=format:%s --after=yesterday.midnight --before=today.midnight --author=$gitname" }
    val commit = exec { "git log --pretty=format:%s" }
    return commit.split("\n")
}

fun createFile(where: File, fileName: String): File {
    val file = File(where, fileName)
    file.createNewFile()
    return file
}

fun createDirectory(folderName: String): File {
    val file = File(folderName)
    file.mkdirs()
    return file
}

fun getGitName(): String {
    return exec { "git config user.name" }
}

fun getCurrentDate(): Calendar {
    return Calendar.getInstance()
}

fun getProjectName(): String {
    return File("").absolutePath
            .replaceBeforeLast("/", "")
            .substring(1)
}

fun exec(command: () -> String): String {
    val args = command().split(" ")
    val output = ProcessExecutor()
            .command(args)
            .readOutput(true)
            .execute()
            .outputUTF8()
//    println(output)
    return output
}

class Template(
        val name: String = "",
        var body: String = ""
)

sealed class Field(val value: String, var args: List<String>) {
    class DateFormatter(
            value: String = "dd MMMM YYYY"
    ) : Field(value, listOf("#dateFormatter = ", "#dateFormatter="))

    class Date(
            value: String = ""
    ) : Field(value, listOf("#date"))

    class Commit(
            value: String = ""
    ) : Field(value, listOf("#commit"))

    class Project(
            value: String = ""
    ) : Field(value, listOf("#project"))
}
