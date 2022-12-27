# Project Work-In-Progress (not yet finished)
# DISCONTINUOUS GALERKIN METHOD FOR THE BIDOMAIN PROBLEM OF CARDIAC ELECTROPHYSIOLOGY

In this project the aim is to simulate the cardiac electrophysiology phenomenon by considering a bidomain 
equation coupled with the Fitz-Hugh Nagumo model. To capture the steep propagation front appearing in cardiac electro-physiology
simulations, DG methods of high order are considered in order to assess the
hyopthesis that a reduced number of degree of freedom are needed to have a good
accuracy with respect to the standare Finite Elements case.

### First test case: Current applied at the center of the domain
![rep_nref=6](https://user-images.githubusercontent.com/119116024/209639892-c7b23642-4133-4fc4-a54e-37a9f19d7079.gif)

### Second test case: Current applied at the lower-left corner of the domain
![left-corner](https://user-images.githubusercontent.com/119116024/209639971-24db584d-94dd-45f0-84cf-47f38a0f3bd6.gif)

### Third test case: Current applied at the left side of the domain
![left-side](https://user-images.githubusercontent.com/119116024/209640011-bf2689f9-376f-4b52-aef6-e738a31ad162.gif)
