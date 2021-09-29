fields{ 
  required {
    ProjectName = String
    Language = ['Golang', 'Java', 'Javascript', 'PHP', 'Python', 'Ruby','N-Able']
    CxCred = String
  }
  optional{
    CxServer = String
    User_Preset = String
    LocationPath = String
    SASTHigh = /[5-9]/
    SASTMedium = /[0-7]/
    SASTLow = /[0-5]/
  }
}
