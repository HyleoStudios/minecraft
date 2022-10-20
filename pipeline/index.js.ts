import * as github from '@actions/github';
import * as core from '@actions/core';
import * as fs from 'fs';

try {
    const context = github.context

    if (context.action != "push") {
        let error = new Error("This action only works on git push");
        throw error;
    }

    const payload = context.payload
    const FILES: Set<any> = new Set();

} catch (err: any) {
    core.setFailed(`Action failed with error ${err}`);
}

function doesExist(file): Boolean {
    return 'added' === file.status || 'modified' === file.status || 'renamed' === file.status;
}

function isGradleProject(path: string) {
    return fs.existsSync(path + '/build.gradle');
}

function previousVersion(project: string): number {
    return 0
}

function incrementVersion(project: string): number {
    return 1
}