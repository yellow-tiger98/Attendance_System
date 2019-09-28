/*
Navicat MySQL Data Transfer

Source Server         : 1
Source Server Version : 50720
Source Host           : localhost:3306
Source Database       : attendance_system

Target Server Type    : MYSQL
Target Server Version : 50720
File Encoding         : 65001

Date: 2019-08-02 19:45:12
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `adminId` varchar(32) NOT NULL,
  `adminName` varchar(32) DEFAULT NULL,
  `adminPwd` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`adminId`),
  KEY `adminName` (`adminName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES ('ce001', '黄达', '123');
INSERT INTO `admin` VALUES ('ce005', '小强', '123');
INSERT INTO `admin` VALUES ('ce011', '小7', '123');

-- ----------------------------
-- Table structure for clock
-- ----------------------------
DROP TABLE IF EXISTS `clock`;
CREATE TABLE `clock` (
  `employeeId` varchar(32) NOT NULL,
  `dayDate` varchar(32) NOT NULL,
  `workUp` varchar(32) DEFAULT NULL,
  `workUpStatus` varchar(32) DEFAULT NULL,
  `workDown` varchar(32) DEFAULT NULL,
  `workDownStatus` varchar(32) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of clock
-- ----------------------------
INSERT INTO `clock` VALUES ('ce002', '2019-07-22', '10:30', '迟到', '18:10', '正常');
INSERT INTO `clock` VALUES ('ce001', '2019-07-23', '09:10', '迟到', '18:30', '正常');
INSERT INTO `clock` VALUES ('ce001', '2019-07-24', '07:30', '正常', '19:30', '正常');
INSERT INTO `clock` VALUES ('ce001', '2019-07-25', '12:00', '旷工', '15:30', '旷工');
INSERT INTO `clock` VALUES ('ce001', '2019-07-26', '09:00', '正常', '17:40', '早退');
INSERT INTO `clock` VALUES ('ce001', '2019-07-22', '09:15', '迟到', '18:10', '正常');
INSERT INTO `clock` VALUES ('ce002', '2019-07-23', '08:15', '正常', '16:30', '早退');
INSERT INTO `clock` VALUES ('ce002', '2019-07-24', '12:30', '旷工', '19:15', '正常');
INSERT INTO `clock` VALUES ('ce002', '2019-07-25', '09:45', '迟到', '14:30', '旷工');
INSERT INTO `clock` VALUES ('ce002', '2019-07-26', '07:09', '正常', '17:45', '早退');
INSERT INTO `clock` VALUES ('ce001', '2019-07-27', '08:36', '正常', '18:10', '正常');
INSERT INTO `clock` VALUES ('ce001', '2019-07-30', null, null, '15:30', '旷工');
INSERT INTO `clock` VALUES ('ce003', '2019-07-30', '20:26', '旷工', '20:26', '正常');
INSERT INTO `clock` VALUES ('ce001', '2019-08-01', '10:38', '迟到', '12:50', '旷工');
INSERT INTO `clock` VALUES ('ce002', '2019-08-01', null, null, '12:45', '旷工');
INSERT INTO `clock` VALUES ('ce003', '2019-08-01', null, null, '12:54', '旷工');
INSERT INTO `clock` VALUES ('ce001', '2019-08-02', '18:44', '旷工', '18:44', '正常');

-- ----------------------------
-- Table structure for employee
-- ----------------------------
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
  `employeeId` varchar(32) NOT NULL,
  `employeeName` varchar(32) NOT NULL,
  `employeeAccount` varchar(32) NOT NULL,
  `employeePwd` varchar(32) NOT NULL,
  `position` varchar(32) NOT NULL,
  `salary` varchar(32) NOT NULL,
  `workDate` varchar(32) NOT NULL,
  `capacity` varchar(32) NOT NULL,
  PRIMARY KEY (`employeeId`),
  KEY `employeeName` (`employeeName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of employee
-- ----------------------------
INSERT INTO `employee` VALUES ('ce001', '黄达', 'huangda', '123', '软件工程师', '10000.00', '2019-08-11', '普通员工');
INSERT INTO `employee` VALUES ('ce002', '小猪', 'zhuzhu', '123', '前端工程师', '10000.00', '2019-08-11', '普通员工');
INSERT INTO `employee` VALUES ('ce003', '小智', 'xiaozhi', '123', '工程师', '20000', '2019-07-30', '普通员工');
INSERT INTO `employee` VALUES ('ce004', '小狼', 'xiaolang', '123', '工程师', '20000', '2019-07-30', '普通员工');
INSERT INTO `employee` VALUES ('ce005', '小强', 'xiaoqiang', '123', '工程师', '20000', '2019-07-30', '管理员');
INSERT INTO `employee` VALUES ('ce006', '小南', 'xiaonan', '123', '工程师', '20000', '20190-08-01', '普通员工');
INSERT INTO `employee` VALUES ('ce010', '小6', 'xiaoliu', '123', '工程师', '20000', '2019-07-25', '普通员工');
INSERT INTO `employee` VALUES ('ce011', '小7', 'xiaoqi', '123', '工程师', '10000', '2019-08-02', '管理员');

-- ----------------------------
-- Table structure for worksheet
-- ----------------------------
DROP TABLE IF EXISTS `worksheet`;
CREATE TABLE `worksheet` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dayDate` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of worksheet
-- ----------------------------
INSERT INTO `worksheet` VALUES ('1', '2019-07-22');
INSERT INTO `worksheet` VALUES ('2', '2019-07-23');
INSERT INTO `worksheet` VALUES ('3', '2019-07-24');
INSERT INTO `worksheet` VALUES ('4', '2019-07-25');
INSERT INTO `worksheet` VALUES ('5', '2019-07-26');
INSERT INTO `worksheet` VALUES ('6', '2019-07-27');
INSERT INTO `worksheet` VALUES ('7', '2019-07-29');
INSERT INTO `worksheet` VALUES ('8', '2019-07-30');
INSERT INTO `worksheet` VALUES ('9', '2019-07-31');
INSERT INTO `worksheet` VALUES ('10', '2019-08-01');
INSERT INTO `worksheet` VALUES ('11', '2019-08-02');
