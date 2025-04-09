package com.example

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepositoryManager
import org.sonarlint.intellij.issue.LiveIssue
import org.sonarlint.intellij.issue.IssueManager
import org.sonarlint.intellij.core.ProjectBindingManager
import org.sonarlint.intellij.analysis.SonarLintAnalyzer
import com.intellij.openapi.project.Project



class CodeReviewAction : AnAction("Run Code Review") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.projecct ?: return
        main()
        Messages.showInfoMessage(project, "Code review has been run!", "Code Review")
        val gitManager = GitRepositoryManager.getInstance(project)
        val repositories = gitManager.repositories
        for (repo in repositories) {
            println("Git root: ${repo.root}")
            println("Current branch: ${repo.currentBranchName}")
        }

        val changeListManager = ChangeListManager.getInstance(project)

        for(change in changeListManager.allChanges){
            println("Current Change  Name: ${change.virtualFile?.name}")
        }
        triggerSonarLintAnalysis(project,changeListManager.getAffectedFiles());

    }
}

fun triggerSonarLintAnalysis(project: Project, files: List<VirtualFile>) {
    val analyzer = SonarLintUtils.get(project, SonarLintAnalyzer::class.java)
    analyzer.analyze(files)
    issueManager.allIssues.mapNotNull { issue ->
        val file = issue.psi?.virtualFile?.path ?: return@mapNotNull null
        IssueEntry(
            filePath = file,
            line = issue.range?.start?.line?.plus(1) ?: -1,
            severity = issue.severity.toString(),
            message = issue.message
        )
    }
}

data class IssueEntry(
    val filePath: String,
    val line: Int,
    val severity: String,
    val message: String
)



fun main() {
    println("Running dummy code review from CodeReviewAction!")
}


fun showIssuesInTable(project: Project, issues: List<IssueEntry>) {
    val columns = arrayOf("File Path", "Line", "Severity", "Message")
    val data = issues.map {
        arrayOf(it.filePath, it.line.toString(), it.severity, it.message)
    }.toTypedArray()

    val table = JBTable(data, columns)
    val scrollPane = JBScrollPane(table)

    val panel = JPanel(BorderLayout())
    panel.add(scrollPane, BorderLayout.CENTER)

    val dialog = DialogWrapper(project)
    object : DialogWrapper(project) {
        init {
            init()
            title = "SonarLint Issues"
        }

        override fun createCenterPanel(): JComponent = panel
    }.show()
}
