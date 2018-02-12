# API XLSX Updater

#### Tool to update clients profile by data in xlsx files

## Description

If you have some data about clients stored in `.xlsx` files then you can update profile on Megaplan by using this tool.

### Authorization

Here is 2 types of authorization (one-time keys is not supported):

1. Call method `APIConnection.authorize (login, password)` with user's `login` and `password`
2. Run application with `-D` flags and then call method `APIConnection.aithorize ()`. 
In this case application will take system properties: `megaplan.api.login` and `megaplan.api.password`, 
and call the first method with read parameters.

```
java ... -Dmegaplan.api.login=... -Dmegaplan.api.password=...
```

To check that application successfully authorized you can call method `APIConnection.isAuthorized ()`

**Remark**: do not forget to set your value of host in `MegaplanAPIManager.CLIENT_HOST` variable

### Sending requests

All requests can be done throw the `APIConnection.sendRequest (action, params)`. 
This method is blocking and will wait until response got from the server 
(To run it asynchronously you may use `Runnable` tasks for some `Executor`).

The first argument `ReqestAction` is enum field that contains URI address to the API script 
(By the fact that answer given as *json* object address shoud ends with `*.api`).

As a result of request you will got an `JSONObject` object 
that contains non-filtered response answer from Megaplan.

### XLSX files

Open `xlsx` file you can with method `XLSXManager.loadWorkbook (path, read)`,
where the second arguments is responsible for calling `XLSXManager.readWorkbook ()`
method in case of success.

Application will read sheet mentioned in `megaplan.xlsx.sheet` system property. If
such parameter isn't specified than will be caused exception.

For reading sheet you will be asked to enter format of this file in console.
After string `Select table format (firts two columns are selected):`
you will need to enter some numbers that are related to profile fields.

To know list of numbers you can write word *"list"*:

```
Select table format (firts two columns are selected):
list [enter]
```

Result will be similar to this:

```
  1. Название                       Name
  2. Название                       CompanyName
  3. Имя                            FirstName
  4. Отчество                       MiddleName
  5. Фамилия                        LastName
  6. День рождения или основания    Birthday
  7. Пол                            Gender
  ...
```

So the format must look like:

```
Select table format (firts two columns are selected):
6 7[enter]
```

**Remark**: the first two columns of `xlsx` filed must be of special format:
1. In the cells of the first column can be anything you want (this will be ignored)
2. In the second column must be placed full names (as in Megaplan) of client.

This columns are already added to the format (you just need to describe the rest columns).

If you've done properly then you will see the model of table that requires a confirmation: `yes` or `no`

```
Select table format (firts two columns are selected):
6 7[enter]
A         B     C         D       
Anything  Name  Birthday  Gender  
Is it right format of table: 
```

When you confirm this model file will be parsed and clients will be loaded to the application.

#### Check for updates

To check data of remote clients' profiles you can call method 
`XLSXManager.findUpdatedProfiles (profiles)`.
As a result you will get a pair of `UserProfile` 
and list of names of fields that is *out-of-date* on the server.

## Requirements

Java version: 
* JRE 8+