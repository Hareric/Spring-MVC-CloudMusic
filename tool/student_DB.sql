
CREATE TABLE student (
  st_no int(11) NOT NULL,
  st_name char(16) NOT NULL,
  st_dept char(20) NOT NULL,
  st_Password char(15) NOT NULL,
  st__emial varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE course(
c_Name char( 15 ) NOT NULL ,
c_credit char( 15 ) NOT NULL ,
c_content varchar( 50 ) NOT NULL ) ENGINE = InnoDB DEFAULT CHARSET = utf8;



INSERT INTO student (st_no, st_name, st_dept, st_Password, st__emial) VALUES
(1, 'Jack', '计算机', '1234', 'Jack@mail.163'),
(2, 'Mark', '信息管理', '3333', 'Mark@mail.163'),
(3, 'Mary', '软件工程', '1111', 'Mary@mail.163'),
(4, 'Rose', '软件工程', '1111', 'rose@mail.163')

INSERT INTO course (c_Name, c_credit, c_content) VALUES
('数据挖掘', '3', '分析数据'),
('算法分析', '3', '学习算法'),
('软件工程', '3', '开发系统')
