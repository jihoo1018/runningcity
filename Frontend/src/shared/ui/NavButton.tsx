import { IconNavLink } from "./IconNavLink";

interface NavButtonProps {
  link: string;
  icon: React.ReactNode;
  label: string;
  onClick?: () => void;
  className: string;
}

const NavButton = ({ link, icon, label, onClick, className }: NavButtonProps) => {
  return (
    <IconNavLink
      to={link}
      icon={icon}
      placement="col"
      gap={4}
      onClick={onClick}
      aria-label={label}
      className={className}
    >
      {label}
    </IconNavLink>
  );
};

export default NavButton;
