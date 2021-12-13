
The _nom-tam-fits_ library is a community-maintained project. We absolutely rely on developers like you to make it better and to keep it going. Whether there is a nagging issue you would like to fix, or a new feature you'd like to see, you can make a difference yourself. We welcome you as a contributor. More than that, we feel like you became part of our community the moment you landed on this page. We very much encourange you to make this project a little bit your own, by submitting pull requests with fixes and enhancement. When you are ready, here are the typical steps for contributing to the project:

1. Old or new __Issue__? Whether you just found a bug, or you are missing a much needed feature, start by checking open [Issues](https://github.com/nom-tam-fits/nom-tam-fits/issues). If an existing issue seems like a good match to yours, feel free to raise your hand and comment on it, to make your voice heard, or to offer help in resolving it. If you find no issues that match, go ahead and create a new one.

2. __Fork__. Is it something you'd like to help resolve? Great! You should start by creating your own fork of the repository so you can work freely on your solution. We also recommend that you place your work on a branch of your fork, which is named either after the issue number, e.g. `issue-192`, or some other descriptive name, such as `implement-foreign-hdu`.

3. __Develop__. Feel free to experiment on your fork/branch. If you run into a dead-end, you can always abandon it (which is why branches are great) and start anew. You can run your own test builds locally using `mvn clean test` before committing your changes. If the tests pass, you should also try running `mvn clean package` to ensure that the javadoc etc. are also in order. Remember to synchronize your `master` branch by fetching changes from upstream every once in a while, and merging them into your development branch. Don't forget to:

   - Add __Javadoc__ your new code. You can keep it sweet and simple, but make sure it properly explains your methods, their arguments and return values, and why an what exceptions may be thrown. You should also cross-reference other methods that are similar, related, or relevant to what you just added.

   - Add __Unit Tests__. Make sure your new code has as close to full unit test coverage as possible. You should aim for 100% diff coverage. When pushing changes to your fork, you can get a coverage report by checking the Github Actions result of your commit (click the Codecov link), and you can analyze what line(s) of code need to have tests added. Try to create tests that are simple but meaningful (i.e. check for valid results, rather than just confirm existing behaior), and try to cover as many realistic scenarios as appropriate. Write lots of tests if you need to. It's OK to write 100 lines of test code for 5 lines of change. Go for it!

4. __Pull Request__. Once you feel your work can be integrated, create a pull request from your fork/branch. You can do that easily from the github page of your fork/branch directly. In the pull request, provide a concise description of what you added or changed. You may get some feedback at this point, and maybe there will be discussions about possible improvements or regressions etc. It's a good thing too, and your changes will likely end up with added polish as a result. You can be all the more proud of it in the end!

5. If all goes well (and why would it not?), your pull-request will get merged, and will be included in the upcoming release of _nom-tam-fits_. Congratulations for your excellent work, and many thanks for dedicating some of your time for making this library a little bit better. There will be many who will appreciate it. :-)


If at any point you have questions, or need feedback, don't be afraid to ask. You can put your questions into the issue you found or created, or your pull-request, or as a Q&amp;A in [Discussions](https://github.com/nom-tam-fits/nom-tam-fits/discussions).



