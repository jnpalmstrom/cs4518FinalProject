## Group Project Proposal

**Team Number:** 1042

**Team Members:** Jack Palmstrom, Evan Plevinsky

### Topic Overview

For our team's final project, the user will be able to take pictures utilizing the phone's camera. They
will then specify whether to send this picture to on on-device or off-device deep learning. The
application then proceeds to run TensorFlow lite, our teams selected deep learning framework, utilizing
a pre-trained model on the image. This pre-trained model is available for both on and off device. On
receiving the results from the model, our application will show the image taken in a RecyclerView,
grouped together with other pictures that fall into a similar category, such as a Daisy and a Rose
being shown in a "Flower" category. This categorization will be based on the top five labels received
from the model. The team will then utilize the hellocharts-android library to show the performance of
the application in a visually appealing way. The charts will show the average time of image
classification, average accuracy of labels, and the most recent times that image classification was run.

### Topic Justification

This application will leverage deep learning performed on the device as well as on a server. As the task
of integrating deep learning into an application becomes easier, the next logical step is to make it 
more efficient and consume less resources to perform. Our application will allow a user to see metrics
captured during the deep learning process such as with image classification accuracy and time to perform
the classification. We will be presenting this data in graphical form to inform the user about metrics
that can be improved upon. This also allows the user to get relatively instant feedback about the 
average performance of these metrics to have a visual representation of what can be improved upon 
in regards to running machine learning in an application.