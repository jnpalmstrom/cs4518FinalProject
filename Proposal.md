## Group Project Proposal

**Team Number:** 1042

**Team Members:** Jack Palmstrom, Evan Plevinsky

### Topic Overview

For our team's final project, 

- User takes pictures
- User has access to server that is off device
- User specifies whether they want to run ML on or off device
- App runs Tensorflow lite pre-trained model on image 
- App shows images in recyclerview that have "similar" classifications based on top 5 labels
- App has chart activities using hellocharts-android library to show performance models. These
performance charts will show avg. time to create image classification on & off device, avg. accuracy
of labels, and 5 most recent times to run image classification on & off device 

### Topic Justification

This app will leverage deep learning performed on the device as well as on a server. As the task of 
integrating deep learning into an application becomes easier, the next logical step is to make it 
more efficient and consume less resources to perform. Our application will allow a user to see metrics
captured during the deep learning process such as image classification accuracy and time to perform
the classification. We will be presenting this data in graphical form to inform the user about metrics
that can be improved upon, and this also allows the user to get relatively instant feedback about the 
average performance of these metrics to have a visual representation of what can be improved upon 
in regards to running machine learning in an application.