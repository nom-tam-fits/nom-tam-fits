# Release HOWTO

This document is meant for package maintainers only. Its purpose is to provide instructions on preparing and releasing updates to the __nom-tam-fits__ library.

## Prerequisites

To release packages, you will need:

 1. admin privileges on the mainline [nom-tam-fits](https://www.github.com/nom-tam-fits/nom-tam-fits) repo.
 
 2. a Nexus Sonatype account. If not register one (see the [guide to publishing packages on Sonatype](https://central.sonatype.org/publish/publish-guide/)). We use the Sonatype Nexus repository for staging releases before pushing them to Maven Central, and also to provide automatic SNAPSHOT releases. (Releases, including SNAPSHOTs are pushed automatically to the Nexus repository by the GitHub Actions CI (`.github/workflows/maven.yml`).
 
 3. push privileges to the `gov.nasa.gsfc.heasarc` repo on Nexus. If you don't already have them, one of the other nom-tam-fits maintainers can request it for you. (It may take up to 2 days after the request to gain access).
 
 4. the `NEXUS_USERNAME` and `NEXUS_PASSWORD` repository secrets on github with your Nexus username and authentication token. At any point, there should be only one of the maintainers publishing packages to Nexus. So use your credentials only if you are that designated person.


## Preparing the release


 * Make sure the CI build on github passes without errors. If the CI build has issues, fix them before proceeding.
 
 * Check that unit tests coverage did not decrease compared to the prior release. (You should not merge pull request in the first place until they maintain or increase test coverage.) Small decrements in overall coverage due to a reduction in the total lines of code are acceptable, as long as the diff coverage itself is equal or above the previous coverage metric. (You should generally insist on 100% diff-coverage, unless there is a good reason why it cannot be attained.) 

 * Clean up and format code, e.g. with Eclipse's Code Cleanup feature to ensure a degree of consistency across the source tree. Use the same formatting rules for subsequent releases. After the code has been cleaned/formatted, run `mvn clean test` to confirm viability before committing.
 
 * Edit `CHANGELOG.md` to summarize the changes for the release, linking entries to issues or pull request as appropriate. Commit and push the updates as necessary. Note any issues pertainig to compatibility at the top of the list of actions.
 
 * Update `pom.xml` with the latest (or best fit) plugin versions. Test them locally with `mvn clean package` before committing and pushing the POM to the repo. Alternatively, if dependabot is generating update PRs, make sure you integrate all that pass the CI.

 * Update version in the rich result section of `src/site/site.xml`.
 
 * Make sure the Project Site is in good shape. Run `mvn clean site`. The open `target/site/index.html` in a web browser. Click through the menu on the left panel and check that all content is current. Check that the changes are properly shown. Check that the _Getting Started_ guide has up-to-date instructions for using the library.
 


## Publishing the release

Once you are confident that everything is in perfect order for the next release, change the version number in `pom.xml`. Remove `-SNAPHOT` from the version. The release needs a proper version number, such a `1.17.2` for finalized release, or something like `1.17.2-rc5` for release candidates and pre-releases. 

 * Commit and push the updated `pom.xml` to the mainline master. Following a successful build, the Github Actions CI will upload the release artifacts to Maven Central Publishing.

 * Log into [Maven Central Publising](https://central.sonatype.com/publishing/) and click the _Deployments_ tab (it might take a few minutes for the upload to appear). If you don't see it within an hour or so of pushing the release commit to GitHub, it's either because Github Actions failed (or did not run at all), or because the CI upload failed. If necessary, retrace your steps and fix what is needed to get the package published with your credentials. 

 * Sleep on it. So far so good, but this is also you last chance to fix anything before the package really goes public, so don't rush it. Take some time to reflect on it, double or triple-check everything, before moving to the next step...

 * On Github, click on _Releases_, and create a new release:
   - Name and tag the release with by the version, such as `1.17.0-rc1` (Note, before 1.17.0 the tag included a 'nom-tam-fits-' prefix as well, which resulted in source tarballs named as nom-tam-fits-nom-tam-fits-<version> -- therefore starting with 1.17.0 we'll omit that).
   - Link the release to the last master commit of the repo.
   - If it is not a final release, be sure to check the box for _pre-release_ near the bottom (when checked the CI will not publish a Github package for this release).
   - Copy/paste the changes for the release from `CHANGELOG.md`.
   - Attach the signed package and javadoc JARs. (use the signed JARs from _Deployment_ on Maven Central Publishing.)
   - After creating the release, delete any prior pre-releases (We should only track final releases in the long run).

 * Maven Central Publishing now give you options to _Release_ to Maven Central or _Drop_ it. If it is a final release, and you are ready to push it to Maven Central, then go ahead and click _Release_. Or, if it's a pre-release, you can drop it.
 
 * Close and unpin any issues on GitHub, which were resolved by the release.
 
 * Close the Milestone on GitHub, and create a new milestone for the next release.
 
 * Clean up Discussions on GitHub. 
 
 * Finally, edit `pom.xml`, and bump the version number and add `-SNAPSHOT`, for example: change the just released `1.17.0` to `1.17.1-SNAPSHOT`. I.e., from here on new commits on master will be part of the 1.17.1 development. Commit and push `pom.xml`. 
 
 
 
