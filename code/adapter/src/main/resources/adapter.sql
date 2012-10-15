create table query(
  id int not null auto_increment,
  network_id bigint not null,
  username varchar(255) not null,
  domain varchar(255) not null,
  query_definition text not null,
  date_created timestamp default current_timestamp,
  constraint query_id_pk primary key(id)
) engine=innodb default charset=latin1;

create table query_result(
  id int not null auto_increment,
  query_id int not null,
  type enum('PATIENTSET','PATIENT_COUNT_XML','PATIENT_AGE_COUNT_XML','PATIENT_RACE_COUNT_XML','PATIENT_VITALSTATUS_COUNT_XML','PATIENT_GENDER_COUNT_XML','ERROR') not null,
  status enum('FINISHED', 'ERROR', 'PROCESSING', 'QUEUED') not null,
  time_elapsed int not null,
  last_updated timestamp default current_timestamp,
  constraint query_result_id_pk primary key(id),
  constraint fk_query_result_query_id foreign key (query_id) references query (id)
) engine=innodb default charset=latin1;

create table error_result(
  id int not null auto_increment,
  result_id int not null,
  message varchar(255) not null,
  constraint error_result_id_pk primary key(id),
  constraint fk_error_result_query_result_id foreign key (result_id) references query_result (id)
) engine=innodb default charset=latin1;

create table count_result(
  id int not null auto_increment,
  result_id int not null,
  original_count int not null,
  obfuscated_count int not null,
  date_created timestamp default current_timestamp,
  constraint count_result_id_pk primary key(id),
  constraint fk_count_result_query_result_id foreign key (result_id) references query_result (id)
) engine=innodb default charset=latin1;

create table breakdown_result(
  id int not null auto_increment,
  result_id int not null,
  data_key varchar(255) not null,
  original_value int not null,
  obfuscated_value int not null,
  constraint breakdown_result_id_pk primary key(id),
  constraint fk_breakdown_result_query_result_id foreign key (result_id) references query_result (id)
) engine=innodb default charset=latin1;

create table patient_set(
  id int not null auto_increment,
  result_id int not null,
  patient_num varchar(255) not null,
  constraint patient_set_id_pk primary key(id),
  constraint fk_patient_set_query_result_id foreign key (result_id) references query_result (id)
) engine=innodb default charset=latin1;