// copy the output from all the triggered jobs by invoking the Copy Artifact plugin for the  
// builds that were triggered  
import hudson.plugins.copyartifact.*
import hudson.model.AbstractBuild
import hudson.Launcher
import hudson.model.BuildListener
import hudson.FilePath 
import jenkins.model.Jenkins

doWork()

def doWork() {
	//String jobPath = new File('config.ini').getText('UTF-8')
	jobPath = getJobNameFromEnv()	
	println 'PathList :' + jobPath
	if(jobPath == null){
		println 'ERROR! empty jobs'
		return
	}
	for (path in jobPath.split(",")){
		jobs = Jenkins.instance.getItemByFullName(path)
		if(jobs != null){
			jobs.each { j ->
			  if (j instanceof com.cloudbees.hudson.plugins.folder.Folder) { return }
			  println('JOB: ' + j.fullName)
			  numbuilds = j.builds.size()
			  if (numbuilds == 0) {
				println('  -> no build')
				return
			  }
			  copyTriggeredResults(j.fullName)		
			}
		}
	}
}

def getTopBuilds(job){
	realCount = getBuildLimitFromEnv()
	//default value is 5
	if(realCount == null){
		realCount = 5
	}
	if(job.builds.size() <= realCount){
		realCount = job.builds.size()
	}
	return job.builds.limit(realCount)
}

def getJobNamesFromArg(){
	jobNames = ''
	for (a in this.args){
		jobNames = jobNames + a + ','
	}
	return jobNames
}

def getJobNameFromEnv(){
	return build.getEnvironment(listener).get('TARGET_LIST')
	//return System.getenv("TARGET_LIST")
}

def getValueFromEnv(KEY){
	return build.getEnvironment(listener).get(KEY)
}

def copyTriggeredResults(projName) {
   def classesRootPath = getValueFromEnv('CLASS_PATH')
   def coverageRootPath = "CoverageFolder"
   def selector = new StatusBuildSelector(true)
   def coverageReportPath = coverageRootPath + "/" + projName + "/";
   println(coverageReportPath)
   
   // CopyArtifact(String projectName, String parameters, BuildSelector selector, String filter, String target, boolean flatten, boolean optional)
   def copyClasses = new CopyArtifact(projName, "", selector, "**/*.class", classesRootPath, true, true)
   def copyCodeCoverage = new CopyArtifact(projName, "", selector, "**/*.ec,**/**.exec", coverageReportPath, true, true)

   // use reflection because direct call invokes deprecated method
   // perform(Build<?, ?> build, Launcher launcher, BuildListener listener)
   def perform = copyClasses.class.getMethod("perform", AbstractBuild, Launcher, BuildListener)
   perform.invoke(copyClasses, build, launcher, listener)
   perform = copyCodeCoverage.class.getMethod("perform", AbstractBuild, Launcher, BuildListener)
   perform.invoke(copyCodeCoverage, build, launcher, listener)
}

  
   
 
   
 