#### How would the application change if patients were grouped and managed by an organisation (identified by an organisation ID)? How would you secure patients' data from access by someone in a different organisation?


We would need to create the structure to deal with the organization - data model, services, etc. Likely this will also come
with some sort of user ACL which will be associated with an Organization. From here, we could define a permission set, 
perhaps in a bitwise way where access to certain patients and devices will be restricted by the ID. We could user levels
which indicate the level of access they may have. For example the hospital administrators can see more of the patient set
than individual practitioners who can only see their current patients.

#### What activity should be audited and how could this be implemented?

A rigorous authentication scheme should be in place if dealing with patient data. Each time a new login takes place,
this should be audited to show who had access to what data. As such, each lookup should be audited. Finally, any changes which
are made to devices and assignments should be audited as well. Using something like an ELK stack we'd be able to log 
lookups and calls and be able to search through them using the elasticsearch implementation.