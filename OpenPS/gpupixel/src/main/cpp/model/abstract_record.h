#pragma once

#include "gpupixel_macros.h"
#include "gpupixel.h"
#include "util.h"
#include <string>

NS_GPUPIXEL_BEGIN

class GPUPIXEL_API AbstractRecord {
public:
  virtual ~AbstractRecord() = default;
  virtual std::string toString() const = 0;
  virtual bool equals(const AbstractRecord& another) const = 0;
  virtual AbstractRecord* clone() const = 0;
};

NS_GPUPIXEL_END