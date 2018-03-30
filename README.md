# Perceptron-based Part-of-Speech Tagger
This is a multiclass-perceptron based Part-of-Speech tagger. It uses several linguistic features to determine the part of speech of a word.
This includes the word itself, the previous and following word, prefixes and suffices, automatically generated lexica, capitalization and the presence
of numbers/colons/punctuation. The data has to be in the [CoNLL format](http://universaldependencies.org/format.html).

# Obtaining the Tagger
The tagger can be built from the source code or a precompiled JAR can be [obtained from GitHub](https://github.com/RobMcH/tagger/releases).

# Usage
The tagger requires the Java Runtime Environment (at least Java 8) to work. It has no graphical user interface and has therefore to be run
from the command line. It accepts several arguments.

java Tagger &lt;PathToTrainingFile> &lt;Options>  
  
-t &lt;PathToTestFile> [OutputPath]: Path to a test file which will be annotated. If [OutputPath] is not specified, the accuracy of the tagger and a confusion "
matrix will be printed.  
-w &lt;OutputPath>: Save the weights of the tagger to the file specified by &lt;OutputPath>.  
-p &lt;OutputPath>: Save the predictions for the training and test file to the file specified by &lt;OutputPath> plus an appended suffix.  
-r &lt;InputPath>: Reads sentences and their annotations from a file in the svm-multiclass format and uses this data to train a model.  
-s &lt;OutputPath>: Saves the sentences enriched with their extracted features in the file given by &lt;OutputPath>.  
